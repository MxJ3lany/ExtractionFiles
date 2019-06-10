package org.fdroid.fdroid.net;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import org.apache.commons.io.input.BoundedInputStream;
import org.fdroid.fdroid.Utils;
import org.fdroid.fdroid.net.bluetooth.BluetoothClient;
import org.fdroid.fdroid.net.bluetooth.BluetoothConnection;
import org.fdroid.fdroid.net.bluetooth.FileDetails;
import org.fdroid.fdroid.net.bluetooth.httpish.Request;
import org.fdroid.fdroid.net.bluetooth.httpish.Response;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

/**
 * Download from a Bluetooth swap repo.  Example URI:
 * {@code bluetooth://84-CF-BF-8B-3E-34/fdroid/repo}
 */
public class BluetoothDownloader extends Downloader {

    private static final String TAG = "BluetoothDownloader";

    public static final String SCHEME = "bluetooth";

    private final BluetoothConnection connection;
    private FileDetails fileDetails;
    private final String sourcePath;

    public static boolean isBluetoothUri(Uri uri) {
        return SCHEME.equals(uri.getScheme())
                && Pattern.matches("([0-9A-F]{2}-)+[0-9A-F]{2}", uri.getHost());
    }

    public BluetoothDownloader(Uri uri, File destFile) throws IOException {
        super(uri, destFile);
        String macAddress = uri.getHost().replace("-", ":");
        this.connection = new BluetoothClient(macAddress).openConnection();
        this.sourcePath = uri.getPath();
    }

    @Override
    protected InputStream getDownloadersInputStream() throws IOException {
        Request request = Request.createGET(sourcePath, connection);
        Response response = request.send();
        fileDetails = response.toFileDetails();

        // TODO: Manage the dependency which includes this class better?
        // Right now, I only needed the one class from apache commons.
        // There are countless classes online which provide this functionality,
        // including some which are available from the Android SDK - the only
        // problem is that they have a funky API which doesn't just wrap a
        // plain old InputStream (the class is ContentLengthInputStream -
        // whereas this BoundedInputStream is much more generic and useful
        // to us).
        BoundedInputStream stream = new BoundedInputStream(response.toContentStream(), fileDetails.getFileSize());
        stream.setPropagateClose(false);

        return stream;
    }

    /**
     * May return null if an error occurred while getting file details.
     */
    @Nullable
    private FileDetails getFileDetails() {
        if (fileDetails == null) {
            Utils.debugLog(TAG, "Going to Bluetooth \"server\" to get file details.");
            try {
                fileDetails = Request.createHEAD(sourcePath, connection).send().toFileDetails();
            } catch (IOException e) {
                Log.e(TAG, "Error getting file details from Bluetooth \"server\"", e);
            }
        }
        return fileDetails;
    }

    @Override
    public boolean hasChanged() {
        FileDetails details = getFileDetails();
        return details == null || details.getCacheTag() == null || details.getCacheTag().equals(getCacheTag());
    }

    @Override
    public long totalDownloadSize() {
        FileDetails details = getFileDetails();
        return details != null ? details.getFileSize() : -1;
    }

    @Override
    public void download() throws IOException, InterruptedException {
        downloadFromStream(false);
        connection.closeQuietly();
    }

    @Override
    protected void close() {
        if (connection != null) {
            connection.closeQuietly();
        }
    }

}
