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

import android.util.Log;

import com.tom_roush.pdfbox.cos.COSDictionary;
import com.tom_roush.pdfbox.cos.COSName;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;

/**
 * Decompresses data encoded using the zlib/deflate compression method,
 * reproducing the original text or binary data.
 *
 * @author Ben Litchfield
 * @author Marcel Kammer
 */
final class FlateFilter extends Filter
{
    private static final int BUFFER_SIZE = 16348;

    @Override
    public DecodeResult decode(InputStream encoded, OutputStream decoded,
                                         COSDictionary parameters, int index) throws IOException
    {
        int predictor = -1;

        final COSDictionary decodeParams = getDecodeParams(parameters, index);
        if (decodeParams != null)
        {
            predictor = decodeParams.getInt(COSName.PREDICTOR);
        }

        try
        {
            if (predictor > 1)
            {
                int colors = Math.min(decodeParams.getInt(COSName.COLORS, 1), 32);
                int bitsPerPixel = decodeParams.getInt(COSName.BITS_PER_COMPONENT, 8);
                int columns = decodeParams.getInt(COSName.COLUMNS, 1);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                decompress(encoded, baos);
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                Predictor.decodePredictor(predictor, colors, bitsPerPixel, columns, bais, decoded);
                decoded.flush();
                baos.reset();
                bais.reset();
            }
            else
            {
                decompress(encoded, decoded);
            }
        } 
        catch (DataFormatException e)
        {
            // if the stream is corrupt a DataFormatException may occur
        	Log.e("PdfBox-Android", "FlateFilter: stop reading corrupt stream due to a DataFormatException");

            // re-throw the exception
            throw new IOException(e);
        }
        return new DecodeResult(parameters);
    }

    // Use Inflater instead of InflateInputStream to avoid an EOFException due to a probably
    // missing Z_STREAM_END, see PDFBOX-1232 for details
    private static void decompress(InputStream in, OutputStream out) throws IOException, DataFormatException 
    { 
        byte[] buf = new byte[2048]; 
        int read = in.read(buf); 
        if (read > 0) 
        { 
            Inflater inflater = new Inflater(); 
            inflater.setInput(buf,0,read); 
            byte[] res = new byte[2048]; 
            while (true) 
            { 
                int resRead = inflater.inflate(res); 
                if (resRead != 0) 
                { 
                    out.write(res,0,resRead); 
                    continue; 
                } 
                if (inflater.finished() || inflater.needsDictionary() || in.available() == 0) 
                {
                    break;
                } 
                read = in.read(buf); 
                inflater.setInput(buf,0,read); 
            }
        }
        out.flush();
    }
    
    @Override
    protected void encode(InputStream input, OutputStream encoded, COSDictionary parameters)
            throws IOException
    {
        DeflaterOutputStream out = new DeflaterOutputStream(encoded);
        int amountRead;
        int mayRead = input.available();
        if (mayRead > 0)
        {
            byte[] buffer = new byte[Math.min(mayRead,BUFFER_SIZE)];
            while ((amountRead = input.read(buffer, 0, Math.min(mayRead,BUFFER_SIZE))) != -1)
            {
                out.write(buffer, 0, amountRead);
            }
        }
        out.close();
        encoded.flush();
    }
}
