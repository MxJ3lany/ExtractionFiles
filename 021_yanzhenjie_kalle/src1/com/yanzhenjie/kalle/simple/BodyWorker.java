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
package com.yanzhenjie.kalle.simple;

import com.yanzhenjie.kalle.Response;
import com.yanzhenjie.kalle.connect.http.Call;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Created by Zhenjie Yan on 2018/2/13.
 */
final class BodyWorker<S, F> extends BasicWorker<SimpleBodyRequest, S, F> {

    private Call mCall;

    BodyWorker(SimpleBodyRequest request, Type succeed, Type failed) {
        super(request, succeed, failed);
    }

    @Override
    protected Response requestNetwork(SimpleBodyRequest request) throws IOException {
        mCall = new Call(request);
        return mCall.execute();
    }

    @Override
    public void cancel() {
        if (mCall != null && !mCall.isCanceled()) {
            mCall.asyncCancel();
        }
    }
}