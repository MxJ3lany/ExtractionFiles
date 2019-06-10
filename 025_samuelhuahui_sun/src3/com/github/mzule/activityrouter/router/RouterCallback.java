package com.github.mzule.activityrouter.router;

import android.content.Context;
import android.net.Uri;

/**
 * Created by Samuel on 4/8/16.
 */
public interface RouterCallback {
    void notFound(Context context, Uri uri);

    boolean beforeOpen(Context context, Uri uri);

    void afterOpen(Context context, Uri uri);

    void error(Context context, Uri uri, Throwable e);
}
