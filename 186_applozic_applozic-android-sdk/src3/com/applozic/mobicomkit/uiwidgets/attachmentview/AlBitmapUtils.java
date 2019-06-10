package com.applozic.mobicomkit.uiwidgets.attachmentview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class AlBitmapUtils {

    private static final int MAX_COMPRESSION_QUALITY = 90;
    private static final String TAG = "AlBitmapUtils";

    public static boolean compress(Uri uri, File file, FragmentActivity fragmentActivity) {
        return compress(uri, file, fragmentActivity, null);
    }

    public static boolean compress(Uri uri, File file, Context context) {
        return compress(uri, file, null, context);

    }

    public static boolean compress(Uri uri, File file, FragmentActivity fragmentActivity, Context context) {

        if (uri == null || file == null) {
            return false;
        }

        byte[] bytes;
        Bitmap scaledBitMap;
        try {

            RequestOptions req = new RequestOptions();
            req.downsample(DownsampleStrategy.AT_MOST);
            Drawable drawable = null;

            if (fragmentActivity != null) {
                drawable = Glide.with(fragmentActivity).load(uri).apply(req).submit(600, 600).get();

            } else if (context != null) {
                drawable = Glide.with(context).load(uri).apply(req).submit(600, 600).get();
            }

            if (drawable == null) {
                return false;
            }
            scaledBitMap = ((BitmapDrawable) drawable).getBitmap();

            if (scaledBitMap != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                scaledBitMap.compress(Bitmap.CompressFormat.JPEG, MAX_COMPRESSION_QUALITY, baos);
                bytes = baos.toByteArray();

                File sizeOfFile = new File(file.getAbsolutePath());

                String path = sizeOfFile.getAbsolutePath();

                if (sizeOfFile.exists()) {
                    boolean deleted = sizeOfFile.delete();
                }

                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(path)));
                bos.write(bytes);
                bos.flush();
                bos.close();

                return true;

            }
        } catch (Exception e) {
            Log.i(TAG, "Got error in compression :" + e.getMessage());
        }
        return false;

    }

}
