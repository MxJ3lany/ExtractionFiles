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

import java.lang.reflect.Field;

public abstract class BaseDataType implements DataMatcher {

    private final SqlType mSqlType;

    public BaseDataType(SqlType sqlType) {
        this.mSqlType = sqlType;
    }

    public SqlType getSqlType() {
        return mSqlType;
    }

    public boolean matches(Field field) {
        Class<?>[] types = getTypes();
        for (Class<?> type : types) {
            if (type.isAssignableFrom(field.getType())) {
                return true;
            }
        }
        return false;
    }
}
