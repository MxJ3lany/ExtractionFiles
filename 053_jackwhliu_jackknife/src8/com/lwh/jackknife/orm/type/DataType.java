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

package com.lwh.jackknife.orm.type;

import com.lwh.jackknife.orm.DataMatcher;

public enum DataType {

    STRING(StringType.getInstance()),
    BOOLEAN(BooleanType.getInstance()),
    CHAR(CharType.getInstance()),
    BYTE(ByteType.getInstance()),
    SHORT(ShortType.getInstance()),
    INT(IntType.getInstance()),
    LONG(LongType.getInstance()),
    FLOAT(FloatType.getInstance()),
    DOUBLE(DoubleType.getInstance()),
    OTHER(ByteArrayType.getInstance());

    private final DataMatcher mMatcher;

    /* package */ DataType(DataMatcher matcher) {
        mMatcher = matcher;
    }

    public DataMatcher getMatcher() {
        return mMatcher;
    }
}
