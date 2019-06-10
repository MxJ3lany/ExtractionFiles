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

package org.sufficientlysecure.keychain.operations.results;

import android.os.Parcel;

public class GenericOperationResult extends OperationResult {
    public GenericOperationResult(int result, OperationLog log) {
        super(result, log);
    }

    public GenericOperationResult(Parcel source) {
        super(source);
    }

    public static final Creator<GenericOperationResult> CREATOR = new Creator<GenericOperationResult>() {
        public GenericOperationResult createFromParcel(final Parcel source) {
            return new GenericOperationResult(source);
        }

        public GenericOperationResult[] newArray(final int size) {
            return new GenericOperationResult[size];
        }
    };
}
