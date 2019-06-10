/*
 * Copyright © 2018 Zhenjie Yan.
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
package com.yanzhenjie.kalle.download;

import com.yanzhenjie.kalle.Canceller;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Created by Zhenjie Yan on 2018/3/18.
 */
public class Work<T extends Download> extends FutureTask<String> implements Canceller {

    private BasicWorker<T> mWorker;
    private final Callback mCallback;

    Work(BasicWorker<T> work, Callback callback) {
        super(work);
        this.mWorker = work;
        this.mCallback = callback;
    }

    @Override
    public void run() {
        mCallback.onStart();
        super.run();
    }

    @Override
    protected void done() {
        try {
            mCallback.onFinish(get());
        } catch (CancellationException e) {
            mCallback.onCancel();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (isCancelled()) {
                mCallback.onCancel();
            } else if (cause != null && cause instanceof Exception) {
                mCallback.onException((Exception) cause);
            } else {
                mCallback.onException(new Exception(cause));
            }
        } catch (Exception e) {
            if (isCancelled()) {
                mCallback.onCancel();
            } else {
                mCallback.onException(e);
            }
        }
        mCallback.onEnd();
    }

    @Override
    public void cancel() {
        cancel(true);
        mWorker.cancel();
    }
}