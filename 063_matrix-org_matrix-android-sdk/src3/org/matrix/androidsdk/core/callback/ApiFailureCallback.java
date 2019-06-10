/*
 * Copyright 2014 OpenMarket Ltd
 * Copyright 2018 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.matrix.androidsdk.core.callback;

import org.matrix.androidsdk.core.model.MatrixError;

/**
 * Callback interface for asynchronously returning API call failures.
 */
public interface ApiFailureCallback extends ErrorCallback {

    /**
     * Called if there is a network error.
     *
     * @param e the exception
     */
    void onNetworkError(Exception e);

    /**
     * Called in case of a Matrix error.
     *
     * @param e the Matrix error
     */
    void onMatrixError(MatrixError e);

}
