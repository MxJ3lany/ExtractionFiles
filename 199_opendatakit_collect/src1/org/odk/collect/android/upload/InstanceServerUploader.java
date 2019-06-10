/*
 * Copyright (C) 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.upload;

import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dto.Instance;
import org.odk.collect.android.http.HttpHeadResult;
import org.odk.collect.android.http.HttpPostResult;
import org.odk.collect.android.http.OpenRosaHttpInterface;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.utilities.ResponseMessageParser;
import org.odk.collect.android.utilities.WebCredentialsUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import timber.log.Timber;

public class InstanceServerUploader extends InstanceUploader {
    private static final String URL_PATH_SEP = "/";

    private final OpenRosaHttpInterface httpInterface;
    private final WebCredentialsUtils webCredentialsUtils;
    private final Map<Uri, Uri> uriRemap;

    public InstanceServerUploader(OpenRosaHttpInterface httpInterface,
                                  WebCredentialsUtils webCredentialsUtils,
                                  Map<Uri, Uri> uriRemap) {
        this.httpInterface = httpInterface;
        this.webCredentialsUtils = webCredentialsUtils;
        this.uriRemap = uriRemap;
    }

    /**
     * Uploads all files associated with an instance to the specified URL. Writes fail/success
     * status to database.
     *
     * Returns a custom success message if one is provided by the server.
     */
    @Override
    public String uploadOneSubmission(Instance instance, String urlString) throws UploadException {
        Uri submissionUri = Uri.parse(urlString);

        long contentLength = 10000000L;

        // We already issued a head request and got a response, so we know it was an
        // OpenRosa-compliant server. We also know the proper URL to send the submission to and
        // the proper scheme.
        if (uriRemap.containsKey(submissionUri)) {
            submissionUri = uriRemap.get(submissionUri);
            Timber.i("Using Uri remap for submission %s. Now: %s", instance.getDatabaseId(),
                    submissionUri.toString());
        } else {
            if (submissionUri.getHost() == null) {
                saveFailedStatusToDatabase(instance);
                throw new UploadException(FAIL + "Host name may not be null");
            }

            URI uri;
            try {
                uri = URI.create(submissionUri.toString());
            } catch (IllegalArgumentException e) {
                saveFailedStatusToDatabase(instance);
                Timber.d(e.getMessage() != null ? e.getMessage() : e.toString());
                throw new UploadException(Collect.getInstance().getString(R.string.url_error));
            }

            HttpHeadResult headResult;
            Map<String, String> responseHeaders;
            try {
                headResult = httpInterface.executeHeadRequest(uri, webCredentialsUtils.getCredentials(uri));
                responseHeaders = headResult.getHeaders();

                if (responseHeaders.containsKey("X-OpenRosa-Accept-Content-Length")) {
                    String contentLengthString = responseHeaders.get("X-OpenRosa-Accept-Content-Length");
                    try {
                        contentLength = Long.parseLong(contentLengthString);
                    } catch (Exception e) {
                        Timber.e(e, "Exception thrown parsing contentLength %s", contentLengthString);
                    }
                }

            } catch (Exception e) {
                saveFailedStatusToDatabase(instance);
                throw new UploadException(FAIL
                        + (e.getMessage() != null ? e.getMessage() : e.toString()));
            }

            if (headResult.getStatusCode() == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                saveFailedStatusToDatabase(instance);
                throw new UploadAuthRequestedException(Collect.getInstance().getString(R.string.server_auth_credentials, submissionUri.getHost()),
                        submissionUri);
            } else if (headResult.getStatusCode() == HttpsURLConnection.HTTP_NO_CONTENT) {
                // Redirect header received
                if (responseHeaders.containsKey("Location")) {
                    try {
                        Uri newURI = Uri.parse(URLDecoder.decode(responseHeaders.get("Location"), "utf-8"));
                        // Allow redirects within same host. This could be redirecting to HTTPS.
                        if (submissionUri.getHost().equalsIgnoreCase(newURI.getHost())) {
                            // Re-add params if server didn't respond with params
                            if (newURI.getQuery() == null) {
                                newURI = newURI.buildUpon()
                                        .encodedQuery(submissionUri.getEncodedQuery())
                                        .build();
                            }
                            uriRemap.put(submissionUri, newURI);
                            submissionUri = newURI;
                        } else {
                            // Don't follow a redirection attempt to a different host.
                            // We can't tell if this is a spoof or not.
                            saveFailedStatusToDatabase(instance);
                            throw new UploadException(FAIL
                                    + "Unexpected redirection attempt to a different host: "
                                    + newURI.toString());
                        }
                    } catch (Exception e) {
                        saveFailedStatusToDatabase(instance);
                        throw new UploadException(FAIL + urlString + " " + e.toString());
                    }
                }
            } else {
                Timber.w("Status code on Head request: %d", headResult.getStatusCode());
                if (headResult.getStatusCode() >= HttpsURLConnection.HTTP_OK
                        && headResult.getStatusCode() < HttpsURLConnection.HTTP_MULT_CHOICE) {
                    saveFailedStatusToDatabase(instance);
                    throw new UploadException(FAIL + "Invalid status code on Head request. If "
                            + "you have a web proxy, you may need to log in to your network.");
                }
            }
        }

        // When encrypting submissions, there is a failure window that may mark the submission as
        // complete but leave the file-to-be-uploaded with the name "submission.xml" and the plaintext
        // submission files on disk.  In this case, upload the submission.xml and all the files in
        // the directory. This means the plaintext files and the encrypted files will be sent to the
        // server and the server will have to figure out what to do with them.
        File instanceFile = new File(instance.getInstanceFilePath());
        File submissionFile = new File(instanceFile.getParentFile(), "submission.xml");
        if (submissionFile.exists()) {
            Timber.w("submission.xml will be uploaded instead of %s", instanceFile.getAbsolutePath());
        } else {
            submissionFile = instanceFile;
        }

        if (!instanceFile.exists() && !submissionFile.exists()) {
            saveFailedStatusToDatabase(instance);
            throw new UploadException(FAIL + "instance XML file does not exist!");
        }

        List<File> files = getFilesInParentDirectory(instanceFile, submissionFile);

        // TODO: when can this happen? It used to cause the whole submission attempt to fail. Should it?
        if (files == null) {
            throw new UploadException("Error reading files to upload");
        }

        HttpPostResult postResult;
        ResponseMessageParser messageParser = new ResponseMessageParser();

        try {
            URI uri = URI.create(submissionUri.toString());

            postResult = httpInterface.uploadSubmissionFile(files, submissionFile, uri,
                    webCredentialsUtils.getCredentials(uri), contentLength);

            int responseCode = postResult.getResponseCode();
            messageParser.setMessageResponse(postResult.getHttpResponse());

            if (responseCode != HttpsURLConnection.HTTP_CREATED && responseCode != HttpsURLConnection.HTTP_ACCEPTED) {
                UploadException exception;
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    exception = new UploadException(FAIL + "Network login failure? Again?");
                } else if (responseCode == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                    exception = new UploadException(FAIL + postResult.getReasonPhrase()
                            + " (" + responseCode + ") at " + urlString);
                } else {
                    if (messageParser.isValid()) {
                        exception = new UploadException(FAIL + messageParser.getMessageResponse());
                    } else {
                        exception = new UploadException(FAIL + postResult.getReasonPhrase()
                                + " (" + responseCode + ") at " + urlString);
                    }

                }
                saveFailedStatusToDatabase(instance);
                throw exception;
            }

        } catch (Exception e) {
            saveFailedStatusToDatabase(instance);
            throw new UploadException(FAIL + "Generic Exception: "
                    + (e.getMessage() != null ? e.getMessage() : e.toString()));
        }

        saveSuccessStatusToDatabase(instance);

        if (messageParser.isValid()) {
            return messageParser.getMessageResponse();
        }

        return null;
    }

    private List<File> getFilesInParentDirectory(File instanceFile, File submissionFile) {
        List<File> files = new ArrayList<>();

        // find all files in parent directory
        File[] allFiles = instanceFile.getParentFile().listFiles();
        if (allFiles == null) {
            return null;
        }

        for (File f : allFiles) {
            String fileName = f.getName();

            if (fileName.startsWith(".")) {
                continue; // ignore invisible files
            } else if (fileName.equals(instanceFile.getName())) {
                continue; // the xml file has already been added
            } else if (fileName.equals(submissionFile.getName())) {
                continue; // the xml file has already been added
            }

            files.add(f);
        }

        return files;
    }


    /**
     * Returns the URL this instance should be submitted to with appended deviceId.
     *
     * If the upload was triggered by an external app and specified an override URL, use that one.
     * Otherwise, use the submission URL configured in the form
     * (https://opendatakit.github.io/xforms-spec/#submission-attributes). Finally, default to the
     * URL configured at the app level.
     */
    @Override
    @NonNull
    public String getUrlToSubmitTo(Instance currentInstance, String deviceId, String overrideURL) {
        String urlString;

        if (overrideURL != null) {
            urlString = overrideURL;
        } else if (currentInstance.getSubmissionUri() != null) {
            urlString = currentInstance.getSubmissionUri().trim();
        } else {
            urlString = getServerSubmissionURL();
        }

        // add deviceID to request
        try {
            urlString += "?deviceID=" + URLEncoder.encode(deviceId != null ? deviceId : "", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Timber.i(e, "Error encoding URL for device id : %s", deviceId);
        }

        return urlString;
    }

    private String getServerSubmissionURL() {

        Collect app = Collect.getInstance();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(
                Collect.getInstance());
        String serverBase = settings.getString(GeneralKeys.KEY_SERVER_URL,
                app.getString(R.string.default_server_url));

        if (serverBase.endsWith(URL_PATH_SEP)) {
            serverBase = serverBase.substring(0, serverBase.length() - 1);
        }

        // NOTE: /submission must not be translated! It is the well-known path on the server.
        String submissionPath = settings.getString(GeneralKeys.KEY_SUBMISSION_URL,
                app.getString(R.string.default_odk_submission));

        if (!submissionPath.startsWith(URL_PATH_SEP)) {
            submissionPath = URL_PATH_SEP + submissionPath;
        }

        return serverBase + submissionPath;
    }
}
