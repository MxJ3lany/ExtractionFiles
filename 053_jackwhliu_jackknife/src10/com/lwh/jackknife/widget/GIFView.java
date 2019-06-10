/*
 * Copyright (C) 2017 The JackKnife Open Source Project
 *
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

package com.lwh.jackknife.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.io.InputStream;

public class GIFView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mSurfaceHolder;
    private Movie mMovie;
    private Handler mHandler = new Handler();
    private float zoom = 1.0f;
    private String path;

    private Runnable r = new Runnable() {
        @Override
        public void run() {
            Canvas canvas = mSurfaceHolder.lockCanvas();
            canvas.save();
            canvas.scale(zoom, zoom);
            mMovie.setTime((int) (System.currentTimeMillis() % mMovie.duration()));
            mMovie.draw(canvas, 0, 0);
            canvas.restore();
            mSurfaceHolder.unlockCanvasAndPost(canvas);
            mHandler.postDelayed(r, 30);
        }
    };

    public GIFView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = 0;
        int height = 0;
        try {
            InputStream inputStream = getContext().getAssets().open(path);
            mMovie = Movie.decodeStream(inputStream);
            width = mMovie.width();
            height = mMovie.height();
            mHandler.post(r);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setMeasuredDimension((int) (width * zoom), (int) (height * zoom));
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHandler.removeCallbacks(r);
    }
}
