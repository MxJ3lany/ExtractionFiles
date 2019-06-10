package org.totschnig.myexpenses.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.totschnig.myexpenses.util.AppDirHelper;
import org.totschnig.myexpenses.util.DistribHelper;
import org.totschnig.myexpenses.util.NougatFileProviderException;

public class SystemImageViewIntentProvider implements ImageViewIntentProvider {
  public Intent getViewIntent(Context context, Uri pictureUri) {
    if (DistribHelper.isBlackberry()) {
      return getFallbackIntent(context, pictureUri);
    }
    try {
      pictureUri = AppDirHelper.ensureContentUri(pictureUri);
    } catch (NougatFileProviderException e) {
      return getFallbackIntent(context, pictureUri);
    }
    Intent intent = instantiateIntent(pictureUri);
    intent.putExtra(Intent.EXTRA_STREAM, pictureUri);
    intent.setDataAndType(pictureUri, "image/jpeg");
    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    return intent;
  }

  private Intent getFallbackIntent(Context context, Uri pictureUri) {
    return new Intent(Intent.ACTION_VIEW, pictureUri, context, SimpleImageActivity.class);
  }

  @NonNull
  protected Intent instantiateIntent(Uri pictureUri) {
    return new Intent(Intent.ACTION_VIEW, pictureUri);
  }
}
