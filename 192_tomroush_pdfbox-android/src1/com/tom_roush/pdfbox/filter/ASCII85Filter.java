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
package com.tom_roush.pdfbox.filter;

import com.tom_roush.pdfbox.cos.COSDictionary;
import com.tom_roush.pdfbox.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Decodes data encoded in an ASCII base-85 representation, reproducing the original binary data.
 * @author Ben Litchfield
 */
final class ASCII85Filter extends Filter
{
    @Override
    public DecodeResult decode(InputStream encoded, OutputStream decoded,
                                         COSDictionary parameters, int index) throws IOException
    {
        ASCII85InputStream is = null;
        try
        {
            is = new ASCII85InputStream(encoded);
            byte[] buffer = new byte[1024];
            int amountRead;
            while((amountRead = is.read(buffer, 0, 1024))!= -1)
            {
                decoded.write(buffer, 0, amountRead);
            }
            decoded.flush();
        }
        finally
        {
            IOUtils.closeQuietly(is);
        }
        return new DecodeResult(parameters);
    }

    @Override
    protected void encode(InputStream input, OutputStream encoded, COSDictionary parameters)
        throws IOException
    {
        ASCII85OutputStream os = new ASCII85OutputStream(encoded);
        byte[] buffer = new byte[1024];
        int amountRead;
        while((amountRead = input.read(buffer, 0, 1024))!= -1)
        {
            os.write(buffer, 0, amountRead);
        }
        os.close();
        encoded.flush();
    }
}
