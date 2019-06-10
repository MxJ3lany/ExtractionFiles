/*
 * Copyright (C) 2019 Peter Gregus for GravityBox Project (C3C076@xda)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ceco.pie.gravitybox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

import android.Manifest.permission;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

public class PickImageActivity extends GravityBoxActivity {

    private static final int REQ_PICK_IMAGE = 1;
    private static final int REQ_CROP_IMAGE = 2;
    private static final String ACTION_CROP = "com.android.camera.action.CROP";

    public static final String EXTRA_CROP_MODE = "cropMode";
    public static final String EXTRA_SCALE = "scale";
    public static final String EXTRA_SCALE_UP = "scaleUpIfNeeded";
    public static final String EXTRA_ASPECT_X = "aspectX";
    public static final String EXTRA_ASPECT_Y = "aspectY";
    public static final String EXTRA_OUTPUT_X = "outputX";
    public static final String EXTRA_OUTPUT_Y = "outputY";
    public static final String EXTRA_SPOTLIGHT_X = "spotlightX";
    public static final String EXTRA_SPOTLIGHT_Y = "spotlightY";
    public static final String EXTRA_FILE_PATH = "filePath";

    private enum CropMode { ORIGINAL, CROP, ASK }

    private ProgressDialog mProgressDialog;
    private CropMode mCropMode = CropMode.ORIGINAL;
    private boolean mScale;
    private boolean mScaleUp;
    private Point mAspectSize;
    private Point mOutputSize;
    private Point mSpotlightSize;
    private Uri mCropUri;
    private AlertDialog mAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (checkSelfPermission(permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, R.string.permission_storage_denied, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mProgressDialog = new ProgressDialog(PickImageActivity.this);
        mProgressDialog.setMessage(getString(R.string.lc_please_wait));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);

        Intent startIntent = getIntent();
        if (savedInstanceState == null && startIntent != null) {
            if (startIntent.hasExtra(EXTRA_CROP_MODE)) {
                mCropMode = CropMode.valueOf(startIntent.getStringExtra(EXTRA_CROP_MODE));
            }
            mScale = startIntent.getBooleanExtra(EXTRA_SCALE, false);
            mScaleUp = startIntent.getBooleanExtra(EXTRA_SCALE_UP, false);
            if (startIntent.hasExtra(EXTRA_ASPECT_X) || startIntent.hasExtra(EXTRA_ASPECT_Y)) {
                mAspectSize = new Point(startIntent.getIntExtra(EXTRA_ASPECT_X, 0),
                        startIntent.getIntExtra(EXTRA_ASPECT_Y, 0));
            }
            if (startIntent.hasExtra(EXTRA_OUTPUT_X) || startIntent.hasExtra(EXTRA_OUTPUT_Y)) {
                mOutputSize = new Point(startIntent.getIntExtra(EXTRA_OUTPUT_X, 0),
                        startIntent.getIntExtra(EXTRA_OUTPUT_Y, 0));
            }
            if (startIntent.hasExtra(EXTRA_SPOTLIGHT_X) || startIntent.hasExtra(EXTRA_SPOTLIGHT_Y)) {
                mSpotlightSize = new Point(startIntent.getIntExtra(EXTRA_SPOTLIGHT_X, 0),
                        startIntent.getIntExtra(EXTRA_SPOTLIGHT_Y, 0));
            }

            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, getString(R.string.imgpick_dialog_title)),
                    REQ_PICK_IMAGE);
        } else {
            finish();
        }
    }

    @Override
    public void onDestroy() {
        dismissProgressDialog();
        dismissAlertDialog();
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQ_PICK_IMAGE) {
                new ImageLoader().execute(data.getData());
            } else if (requestCode == REQ_CROP_IMAGE) {
                if (data.getData() != null) {
                    sendResult(data.getData());
                } else {
                    Toast.makeText(this, R.string.imgpick_crop_uri_null, Toast.LENGTH_LONG).show();
                    sendResult(mCropUri);
                }
            }
        } else {
            sendResult(null);
        }
    }

    private void onImageLoadedResult(final LoadResult result) {
        if (isDestroyed()) {
            return;
        } else if (result.exception != null) {
            Toast.makeText(this, String.format("%s: %s", getString(R.string.imgpick_choose_error),
                    result.exception.getMessage()), Toast.LENGTH_LONG).show();
            sendResult(null);
            return;
        }

        if (mCropMode == CropMode.ORIGINAL) {
            setResult(Activity.RESULT_OK, 
                    new Intent().putExtra(EXTRA_FILE_PATH, result.file.getAbsolutePath()));
            finish();
        } else if (mCropMode == CropMode.CROP) {
            cropImage(result.file);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(
                    this, isThemeDark() ? R.style.AlertDialogStyleDark : R.style.AlertDialogStyle))
                .setTitle(R.string.imgpick_crop_ask_title)
                .setMessage(getString(R.string.imgpick_crop_ask_msg, mOutputSize.x, mOutputSize.y))
                .setPositiveButton(R.string.yes, (dialog, which) -> cropImage(result.file))
                .setNegativeButton(R.string.no, (dialog, which) -> {
                    setResult(Activity.RESULT_OK,
                            new Intent().putExtra(EXTRA_FILE_PATH, result.file.getAbsolutePath()));
                    finish();
                });
            mAlertDialog = builder.create();
            mAlertDialog.show();
        }
    }

    private void sendResult(Uri uri) {
        if (uri == null) {
            setResult(Activity.RESULT_CANCELED);
        } else {
            try {
                File f = createFileFromUri(uri, false);
                setResult(Activity.RESULT_OK, 
                        new Intent().putExtra(EXTRA_FILE_PATH, f.getAbsolutePath()));
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, String.format("%s: %s", getString(R.string.imgpick_crop_error),
                        e.getMessage()), Toast.LENGTH_LONG).show();
                setResult(Activity.RESULT_CANCELED);
            }
            getContentResolver().delete(uri, null, null);
        }
        if (mCropUri != null && mCropUri != uri) {
            getContentResolver().delete(mCropUri, null, null);
            mCropUri = null;
        }
        finish();
    }

    private File createFileFromUri(Uri uri, boolean downscale) throws Exception {
        File outFile = new File(Utils.getCacheDir(PickImageActivity.this) + "/" + UUID.randomUUID().toString());
        InputStream in = null;
        FileOutputStream out = null;
        BitmapFactory.Options options = null;
        try {
            if (downscale) {
                // downscale to maxWidth/maxHeight of display
                in = getContentResolver().openInputStream(uri);
                options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(in, null, options);
                options.inSampleSize = BitmapUtils.calculateInSampleSize(
                        options, getMaxWidth(), getMaxHeight());
                options.inJustDecodeBounds = false;
                in.close();
            }

            // save bitmap
            in = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(in, null, options);
            out = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            bitmap.recycle();
        } finally {
            try { in.close(); } catch (Exception ignored) { }
            try { out.close(); } catch (Exception ignored) { }
        }
        return outFile;
    }

    private int getMaxWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    private int getMaxHeight() {
        return getResources().getDisplayMetrics().heightPixels;
    }

    private void cropImage(File file) {
        try {
            mCropUri = Uri.parse(Images.Media.insertImage(
                    getContentResolver(), file.getAbsolutePath(), null, null));
            Intent cropIntent = new Intent(ACTION_CROP);
            cropIntent.setDataAndType(mCropUri, "image/*");
            cropIntent.putExtra("crop", "true");
            if (mAspectSize != null) {
                cropIntent.putExtra(EXTRA_ASPECT_X, mAspectSize.x);
                cropIntent.putExtra(EXTRA_ASPECT_Y, mAspectSize.y);
            }
            if (mOutputSize != null) {
                cropIntent.putExtra(EXTRA_OUTPUT_X, mOutputSize.x);
                cropIntent.putExtra(EXTRA_OUTPUT_Y, mOutputSize.y);
            }
            if (mSpotlightSize != null) {
                cropIntent.putExtra(EXTRA_SPOTLIGHT_X, mSpotlightSize.x);
                cropIntent.putExtra(EXTRA_SPOTLIGHT_Y, mSpotlightSize.y);
            }
            cropIntent.putExtra(EXTRA_SCALE, mScale);
            cropIntent.putExtra(EXTRA_SCALE_UP, mScaleUp);
            cropIntent.putExtra("return-data", false);
            cropIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            startActivityForResult(cropIntent, REQ_CROP_IMAGE);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, String.format("%s: %s", getString(R.string.imgpick_crop_error),
                    e.getMessage()), Toast.LENGTH_LONG).show();
            sendResult(null);
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private void dismissAlertDialog() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }

    class LoadResult {
        File file;
        Exception exception;
    }

    class ImageLoader extends AsyncTask<Uri, Integer, LoadResult> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
        }

        @Override
        protected LoadResult doInBackground(Uri... params) {
            LoadResult result = new LoadResult();
            try { 
                result.file = createFileFromUri(params[0], true);
            } catch (Exception e) {
                result.exception = e;
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(LoadResult result) {
            dismissProgressDialog();
            onImageLoadedResult(result);
        }
    }
}
