/*
 * Copyright 2015 Yan Zhenjie
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
package com.yanzhenjie.nohttp.error;

/**
 * <p>Network error when requested.</p>
 * Created in 2016/2/25 9:48.
 *
 * @author Yan Zhenjie.
 */
public class NetworkError extends Exception {

    private static final long serialVersionUID = 11548468L;

    public NetworkError() {
    }

    public NetworkError(String detailMessage) {
        super(detailMessage);
    }

    public NetworkError(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public NetworkError(Throwable throwable) {
        super(throwable);
    }
}
