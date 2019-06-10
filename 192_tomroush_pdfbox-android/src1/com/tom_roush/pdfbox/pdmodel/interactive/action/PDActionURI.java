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
package com.tom_roush.pdfbox.pdmodel.interactive.action;

import com.tom_roush.pdfbox.cos.COSDictionary;

/**
 * This represents a URI action that can be executed in a PDF document.
 *
 * @author Ben Litchfield
 * @author Panagiotis Toumasis
 */
public class PDActionURI extends PDAction
{
    /**
     * This type of action this object represents.
     */
    public static final String SUB_TYPE = "URI";

    /**
     * Default constructor.
     */
    public PDActionURI()
    {
        action = new COSDictionary();
        setSubType(SUB_TYPE);
    }

    /**
     * Constructor.
     *
     * @param a The action dictionary.
     */
    public PDActionURI(COSDictionary a)
    {
        super(a);
    }

    /**
     * This will get the type of action that the actions dictionary describes.
     * It must be URI for a URI action.
     *
     * @return The S entry of the specific URI action dictionary.
     */
    public String getS()
    {
        return action.getNameAsString("S");
    }

    /**
     * This will set the type of action that the actions dictionary describes.
     * It must be URI for a URI action.
     *
     * @param s The URI action.
     */
    public void setS(String s)
    {
        action.setName("S", s);
    }

    /**
     * This will get the uniform resource identifier to resolve, encoded in
     * 7-bit ASCII.
     *
     * @return The URI entry of the specific URI action dictionary.
     */
    public String getURI()
    {
        return action.getString("URI");
    }

    /**
     * This will set the uniform resource identifier to resolve, encoded in
     * 7-bit ASCII.
     *
     * @param uri The uniform resource identifier.
     */
    public void setURI(String uri)
    {
        action.setString("URI", uri);
    }

    /**
     * This will specify whether to track the mouse position when the URI is
     * resolved. Default value: false. This entry applies only to actions
     * triggered by the user's clicking an annotation; it is ignored for actions
     * associated with outline items or with a document's OpenAction entry.
     *
     * @return A flag specifying whether to track the mouse position when the
     * URI is resolved.
     */
    public boolean shouldTrackMousePosition()
    {
        return this.action.getBoolean("IsMap", false);
    }

    /**
     * This will specify whether to track the mouse position when the URI is
     * resolved.
     *
     * @param value The flag value.
     */
    public void setTrackMousePosition(boolean value)
    {
        this.action.setBoolean("IsMap", value);
    }
}
