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
 * This class will take a dictionary and determine which type of action to create.
 *
 * @author Ben Litchfield
 */
public final class PDActionFactory
{
    /**
     * Utility Class.
     */
    private PDActionFactory()
    {
        //utility class
    }

    /**
     * This will create the correct type of action based on the type specified
     * in the dictionary.
     *
     * @param action An action dictionary.
     *
     * @return An action of the correct type.
     */
    public static PDAction createAction( COSDictionary action )
    {
        PDAction retval = null;
        if( action != null )
        {
            String type = action.getNameAsString( "S" );
            if( PDActionJavaScript.SUB_TYPE.equals( type ) )
            {
                retval = new PDActionJavaScript( action );
            }
            else if( PDActionGoTo.SUB_TYPE.equals( type ) )
            {
                retval = new PDActionGoTo( action );
            }
            else if( PDActionLaunch.SUB_TYPE.equals( type ) )
            {
                retval = new PDActionLaunch( action );
            }
            else if( PDActionRemoteGoTo.SUB_TYPE.equals( type ) )
            {
                retval = new PDActionRemoteGoTo( action );
            }
            else if( PDActionURI.SUB_TYPE.equals( type ) )
            {
                retval = new PDActionURI( action );
            }
            else if (PDActionNamed.SUB_TYPE.equals(type))
            {
            	retval = new PDActionNamed(action);
            }
        }
        return retval;
    }

}
