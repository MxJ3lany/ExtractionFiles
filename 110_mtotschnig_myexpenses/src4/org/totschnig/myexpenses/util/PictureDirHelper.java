package org.totschnig.myexpenses.util;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.ContextCompat;

import org.totschnig.myexpenses.MyApplication;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.totschnig.myexpenses.util.AppDirHelper.getContentUriForFile;

public class PictureDirHelper {
  /**
   * create a File object for storage of picture data
   *
   * @param temp if true the returned file is suitable for temporary storage while
   *             the user is editing the transaction if false the file will serve
   *             as permanent storage,
   *             care is taken that the file does not yet exist
   * @return a file on the external storage
   */
  public static File getOutputMediaFile(String fileName, boolean temp, boolean secure) {
    // To be safe, you should check that the SDCard is mounted
    // using Environment.getExternalStorageState() before doing this.

    File mediaStorageDir = temp ? AppDirHelper.getCacheDir() : getPictureDir(secure);
    if (mediaStorageDir == null) return null;
    int postfix = 0;
    File result;
    do {
      result = new File(mediaStorageDir, getOutputMediaFileName(fileName, postfix));
      postfix++;
    } while (result.exists());
    return result;
  }

  public static Uri getOutputMediaUri(boolean temp) {
    return getOutputMediaUri(temp, MyApplication.getInstance().isProtected());
  }

  public static Uri getOutputMediaUri(boolean temp, boolean secure) {
    String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        .format(new Date());
    File outputMediaFile = getOutputMediaFile(fileName, temp, secure);
    if (outputMediaFile == null) return null;
    if (!temp) {
      try {
        return getContentUriForFile(outputMediaFile);
      } catch (IllegalArgumentException e) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
          throw new NougatFileProviderException(e);
        }
      }
    }
    return Uri.fromFile(outputMediaFile);
  }

  public static String getPictureUriBase(boolean temp) {
    Uri sampleUri = getOutputMediaUri(temp);
    if (sampleUri == null) return null;
    String uriString = sampleUri.toString();
    return uriString.substring(0, uriString.lastIndexOf('/'));
  }

  private static String getOutputMediaFileName(String base, int postfix) {
    if (postfix > 0) {
      base += "_" + postfix;
    }
    return base + ".jpg";
  }

  public static File getPictureDir(boolean secure) {
    File result;
    if (secure) {
      result = new File(MyApplication.getInstance().getFilesDir(), "images");
    } else {
      //https://stackoverflow.com/a/43497841/1199911
      result = ContextCompat.getExternalFilesDirs(MyApplication.getInstance(), Environment.DIRECTORY_PICTURES)[0];
    }
    if (result == null) return null;
    result.mkdir();
    return result.exists() ? result : null;
  }

  @SuppressLint("InlinedApi")
  public static String getContentIntentAction() {
    return Intent.ACTION_GET_CONTENT;
  }

  /**
   * @param pictureUri
   * @return
   */
  public static boolean doesPictureExist(Uri pictureUri) throws IllegalArgumentException {
    return getFileForUri(pictureUri).exists();
  }

  @VisibleForTesting
  @NonNull
  public static File getFileForUri(Uri pictureUri) throws IllegalArgumentException {
    if ("file".equals(pictureUri.getScheme())) {
      return new File(pictureUri.getPath());
    }
    Preconditions.checkArgument("authority", AppDirHelper.getFileProviderAuthority(),
        pictureUri.getAuthority());
    List<String> pathSegments = pictureUri.getPathSegments();
    //TODO create unit test for checking if this logic is in sync with image_path.xml
    String pathDomain = pathSegments.get(0);
    switch (pathDomain) {
      case "external-files":
        Preconditions.checkArgument("directory", Environment.DIRECTORY_PICTURES, pathSegments.get(1));
        return new File(getPictureDir(false), pathSegments.get(2));
      case "images":
        return new File(getPictureDir(true), pathSegments.get(1));
      default:
        //we are deliberately ignoring files from cache dirs here, since they are not supposed to be persisted
        throw new IllegalArgumentException(String.format(Locale.ROOT, "Unable to handle %s", pathDomain));
    }
  }
}
