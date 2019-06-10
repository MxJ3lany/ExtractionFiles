/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tom_roush.pdfbox.pdmodel.font;

import android.graphics.Path;

import java.io.IOException;

/**
 * A vector outline font, e.g. not Type 3.
 *
 * @author John Hewson
 */
public interface PDVectorFont
{
    /**
     * Returns the glyph path for the given character code.
     *
     * @param code character code
     * @throws java.io.IOException if the font could not be read
     */
    Path getPath(int code) throws IOException;

    /**
     * Returns true if this font contains a glyph for the given character code.
     *
     * @param code character code
     */
    boolean hasGlyph(int code) throws IOException;
}
