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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.lwh.jackknife.orm.exception.OrmStateException;

public class Orm {

    private static SQLiteDatabase sDatabase;
    private static SQLiteOpenHelper sHelper;
    private static int STATE_DATABASE_NOT_EXISTS = -1;
    private static int STATE_DATABASE_EXISTS = 0;
    private static int STATE_DATABASE_UPDATING = 1;
    private static int sDatabaseState = STATE_DATABASE_NOT_EXISTS;

    public static boolean isPrepared() {
        return sDatabaseState == STATE_DATABASE_EXISTS;
    }

    public static boolean isWaitingUpdate() {
        return sDatabaseState == STATE_DATABASE_UPDATING;
    }

    public static void update() {
        sDatabaseState = STATE_DATABASE_UPDATING;
    }

    public static SQLiteDatabase getDatabase() {
        if (isPrepared()) {
            return sDatabase;
        } else if (isWaitingUpdate()) {
            sDatabase = sHelper.getWritableDatabase();
            if (sDatabase != null) {
                sDatabaseState = STATE_DATABASE_EXISTS;
            }
            return sDatabase;
        } else throw new OrmStateException("Database is not exists.");
    }

    public synchronized static void init(Context context, String databaseName) {
        sHelper = new OrmSQLiteOpenHelper(context, databaseName, 1, null);
        sDatabase = sHelper.getWritableDatabase();
        if (sDatabase != null) {
            sDatabaseState = STATE_DATABASE_EXISTS;
        }
    }

    public synchronized static void init(Context context, OrmConfig config) {
        String name = config.getDatabaseName();
        int versionCode = config.getVersionCode();
        Class<? extends OrmTable>[] tables = config.getTables();
        sHelper = new OrmSQLiteOpenHelper(context, name, versionCode, tables);
        sDatabase = sHelper.getWritableDatabase();
        if (sDatabase != null) {
            sDatabaseState = STATE_DATABASE_EXISTS;
        }
    }
}
