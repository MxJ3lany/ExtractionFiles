/*
 * Copyright (C) 2017 Schürmann & Breitmoser GbR
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.sufficientlysecure.keychain.keyimport.processing;

import org.sufficientlysecure.keychain.operations.results.OperationResult;

/**
 * The AsyncTaskResultWrapper is used to wrap a result from a AsyncTask (for example: Loader).
 * You can pass the result and an exception in it if an error occurred.
 * Concept found at:
 * https://stackoverflow.com/questions/19593577/how-to-handle-errors-in-custom-asynctaskloader
 *
 * @param <T> - Typ of the result which is wrapped
 */
public class AsyncTaskResultWrapper<T> {

    private final T mResult;
    private final OperationResult mOperationResult;

    public AsyncTaskResultWrapper(T result, OperationResult operationResult) {
        this.mResult = result;
        this.mOperationResult = operationResult;
    }

    public T getResult() {
        return mResult;
    }

    public OperationResult getOperationResult() {
        return mOperationResult;
    }

}
