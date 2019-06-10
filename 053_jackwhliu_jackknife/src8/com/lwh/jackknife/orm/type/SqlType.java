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

/**
 * SQL of the SQLite database is enumerated, and the data types of all other database SQL statements
 * will eventually become the following five.
 */
public enum SqlType {

    /**
     * The value is the null value.
     */
    NULL,

    /**
     * The value is a signed integer that is stored at 1, 2, 3, 4, 6, or 8 bytes depending on the
     * size of the value.
     */
    INTEGER,

    /**
     * Values are floating-point Numbers, stored in 8-byte IEEE floating point Numbers.
     */
    REAL,

    /**
     * The value is a text string and is stored using database encoding
     * (utf-8, utf-16be or utf-16le).
     */
    TEXT,

    /**
     * A value is a block of data that is stored as it is typed.
     */
    BLOB
}
