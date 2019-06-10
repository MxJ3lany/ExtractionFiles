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
package com.tom_roush.pdfbox.pdmodel.graphics.state;

/**
 * Rendering intent.
 *
 * @author John Hewson
 */
public enum RenderingIntent
{
    /**
     * Absolute Colorimetric.
     */
    ABSOLUTE_COLORIMETRIC("AbsoluteColorimetric"),

    /**
     * Relative Colorimetric.
     */
    RELATIVE_COLORIMETRIC("RelativeColorimetric"),

    /**
     * Saturation.
     */
    SATURATION("Saturation"),

    /**
     * Perceptual
     */
    PERCEPTUAL("Perceptual");

    public static RenderingIntent fromString(String value)
    {
        if (value.equals("AbsoluteColorimetric"))
        {
            return ABSOLUTE_COLORIMETRIC;
        }
        else if (value.equals("RelativeColorimetric"))
        {
            return RELATIVE_COLORIMETRIC;
        }
        else if (value.equals("Saturation"))
        {
            return SATURATION;
        }
        else if (value.equals("Perceptual"))
        {
            return PERCEPTUAL;
        }
        throw new IllegalArgumentException(value);
    }

    private final String value;

    RenderingIntent(String value)
    {
        this.value = value;
    }

    /**
     * Returns the string value, as used in a PDF file.
     */
    public String stringValue()
    {
        return value;
    }
}
