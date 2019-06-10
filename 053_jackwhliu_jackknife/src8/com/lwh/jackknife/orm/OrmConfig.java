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

package com.lwh.jackknife.orm;

import android.text.TextUtils;

public class OrmConfig {

    private String mDatabaseName;
    private int mVersionCode;
    private Class<? extends OrmTable>[] mTables;

    private OrmConfig(Builder builder) {
        mDatabaseName = builder.mDatabaseName;
        mVersionCode = builder.mVersionCode;
        mTables = builder.mTables;
    }

    public String getDatabaseName() {
        return mDatabaseName;
    }

    public int getVersionCode() {
        return mVersionCode;
    }

    public Class<? extends OrmTable>[] getTables() {
        return mTables;
    }

    public static class Builder {

        private String mDatabaseName;
        private int mVersionCode = 1;
        private Class<? extends OrmTable>[] mTables;

        public Builder database(String name) {
            mDatabaseName = name;
            return this;
        }

        public Builder version(int code) {
            mVersionCode = code;
            return this;
        }

        public Builder tables(Class<? extends OrmTable>... tables) {
            mTables = tables;
            return this;
        }

        public OrmConfig build() {
            if (!TextUtils.isEmpty(mDatabaseName)) {
                return new OrmConfig(this);
            }
            return null;
        }
    }
}
