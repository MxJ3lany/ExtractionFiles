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
package com.tom_roush.pdfbox.rendering;

import android.graphics.Path;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.tom_roush.pdfbox.pdmodel.font.PDCIDFontType0;

/**
 * GeneralPath conversion for CFF CIDFont.
 *
 * @author John Hewson
 */
final class CIDType0Glyph2D implements Glyph2D
{
    private final Map<Integer, Path> cache = new HashMap<Integer, Path>();
    private final PDCIDFontType0 font;
    private final String fontName;
    /**
     * Constructor.
     *
     * @param font Type 0 CIDFont
     */
    CIDType0Glyph2D(PDCIDFontType0 font) // todo: what about PDCIDFontType2?
    {
        this.font = font;
        fontName = font.getBaseFont();
    }
    @Override
    public Path getPathForCharacterCode(int code)
    {
        if (cache.containsKey(code))
        {
            return cache.get(code);
        }
        try
        {
            if (!font.hasGlyph(code))
            {
                int cid = font.getParent().codeToCID(code);
                String cidHex = String.format("%04x", cid);
                Log.w("PdfBox-Android", "No glyph for " + code + " (CID " + cidHex + ") in font " + fontName);
            }
            Path path = font.getPath(code);
            cache.put(code, path);
            return path;
        }
        catch (IOException e)
        {
            // TODO: escalate this error?
            Log.w("PdfBox-Android", "Glyph rendering failed", e);
            return new Path();
        }
    }
    @Override
    public void dispose()
    {
        cache.clear();
    }
}
