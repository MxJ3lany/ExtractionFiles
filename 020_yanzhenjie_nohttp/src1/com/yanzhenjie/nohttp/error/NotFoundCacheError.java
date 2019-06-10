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
 * Created in 2016/3/3 13:34.
 *
 * @author Yan Zhenjie.
 */
public class NotFoundCacheError extends Exception {

    private static final long serialVersionUID = 115481468L;

    public NotFoundCacheError() {
    }

    public NotFoundCacheError(String detailMessage) {
        super(detailMessage);
    }

    public NotFoundCacheError(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public NotFoundCacheError(Throwable throwable) {
        super(throwable);
    }
}
