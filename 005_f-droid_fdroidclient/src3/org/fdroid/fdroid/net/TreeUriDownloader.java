package org.fdroid.fdroid.net;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;
import org.fdroid.fdroid.FDroidApp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;

/**
 * An {@link Downloader} subclass for downloading files from a repo on a
 * removable storage device like an SD Card or USB OTG thumb drive using the
 * Storage Access Framework.  Permission must first be granted by the user via a
 * {@link android.content.Intent#ACTION_OPEN_DOCUMENT_TREE} or
 * {@link android.os.storage.StorageVolume#createAccessIntent(String)}request,
 * then F-Droid will have permanent access to that{@link android.net.Uri}.
 * <p>
 * The base repo URL of such a repo looks like:
 * {@code content://com.android.externalstorage.documents/tree/1AFB-2402%3A/document/1AFB-2402%3Atesty.at.or.at%2Ffdroid%2Frepo}
 *
 * @see android.support.v4.provider.DocumentFile#fromTreeUri(Context, Uri)
 * @see <a href="https://developer.android.com/guide/topics/providers/document-provider.html">Open Files using Storage Access Framework</a>
 * @see <a href="https://developer.android.com/training/articles/scoped-directory-access.html">Using Scoped Directory Access</a>
 */
@TargetApi(21)
public class TreeUriDownloader extends Downloader {
    public static final String TAG = "TreeUriDownloader";

    /**
     * Whoever designed this {@link android.provider.DocumentsContract#isTreeUri(Uri) URI system}
     * was smoking crack, it escapes <b>part</b> of the URI path, but not all.
     * So crazy tricks are required.
     */
    public static final String ESCAPED_SLASH = "%2F";

    private final Context context;
    private final Uri treeUri;
    private final DocumentFile documentFile;

    TreeUriDownloader(Uri uri, File destFile)
            throws FileNotFoundException, MalformedURLException {
        super(uri, destFile);
        context = FDroidApp.getInstance();
        String path = uri.getEncodedPath();
        int lastEscapedSlash = path.lastIndexOf(ESCAPED_SLASH);
        String pathChunkToEscape = path.substring(lastEscapedSlash + ESCAPED_SLASH.length());
        String escapedPathChunk = Uri.encode(pathChunkToEscape);
        treeUri = uri.buildUpon().encodedPath(path.replace(pathChunkToEscape, escapedPathChunk)).build();
        documentFile = DocumentFile.fromTreeUri(context, treeUri);
    }

    /**
     * This needs to convert {@link FileNotFoundException} and
     * {@link IllegalArgumentException} to {@link ProtocolException} since the mirror
     * failover logic expects network errors, not filesystem or other errors.
     * In the downloading logic, filesystem errors are related to the file as it is
     * being downloaded and written to disk.  Things can fail here if the USB stick is
     * not longer plugged in, the files were deleted by some other process, etc.
     * <p>
     * Example: {@code IllegalArgumentException: Failed to determine if
     * 6EED-6A10:guardianproject.info/wind-demo/fdroid/repo/index-v1.jar is child of
     * 6EED-6A10:: java.io.File NotFoundException: No root for 6EED-6A10}
     * <p>
     * Example:
     */
    @Override
    protected InputStream getDownloadersInputStream() throws IOException {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(treeUri);
            if (inputStream == null) {
                return null;
            } else {
                return new BufferedInputStream(inputStream);
            }
        } catch (FileNotFoundException | IllegalArgumentException e) {
            throw new ProtocolException(e.getLocalizedMessage());
        }
    }

    @Override
    public boolean hasChanged() {
        return true;  // TODO how should this actually be implemented?
    }

    @Override
    protected long totalDownloadSize() {
        return documentFile.length();
    }

    @Override
    public void download() throws IOException, InterruptedException {
        downloadFromStream(false);
    }

    @Override
    protected void close() {
    }
}
