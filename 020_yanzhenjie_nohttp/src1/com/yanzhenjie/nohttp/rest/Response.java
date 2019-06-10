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
package com.yanzhenjie.nohttp.rest;

/**
 * <p>Http response, Including header information and response packets.</p>
 * Created in Oct 15, 2015 8:55:37 PM.
 *
 * @param <T> a generic, on behalf of you can accept the result type,.It should be with the {@link Request},
 * {@link OnResponseListener}.
 * @author Yan Zhenjie.
 */

import com.yanzhenjie.nohttp.Headers;

/**
 * <p>Http response, Including header information and response packets.</p>
 * Created in Oct 15, 2015 8:55:37 PM.
 *
 * @param <T> The handle data type, it should be with the {@link Request}, {@link OnResponseListener}.
 * @author Yan Zhenjie.
 */
public interface Response<T> {

    /**
     * Get the Request.
     */
    Request<T> request();

    /**
     * Get the response code of handle.
     *
     * @return response code.
     */
    int responseCode();

    /**
     * Request is executed successfully.
     *
     * @return True: Succeed, false: failed.
     */
    boolean isSucceed();

    /**
     * Whether the data returned from the cache.
     *
     * @return True: the data from cache, false: the data from server.
     */
    boolean isFromCache();

    /**
     * Get http response headers.
     *
     * @return {@link Headers}.
     */
    Headers getHeaders();

    /**
     * Get handle results.
     *
     * @return {@link T}.
     */
    T get();

    /**
     * When the handle fail to getList the exception type.
     *
     * @return The exception.
     */
    Exception getException();

    /**
     * Gets the tag of handle.
     *
     * @return {@link Object}.
     */
    Object getTag();

    /**
     * Gets the millisecond of handle.
     *
     * @return {@link Long}.
     */
    long getNetworkMillis();
}