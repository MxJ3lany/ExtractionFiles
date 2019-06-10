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
import com.tom_roush.pdfbox.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Decompresses data encoded using a DCT (discrete cosine transform)
 * technique based on the JPEG standard.
 *
 * @author John Hewson
 */
final class DCTFilter extends Filter
{
    @Override
    public DecodeResult decode(InputStream encoded, OutputStream decoded,
                                         COSDictionary parameters, int index) throws IOException
    {
    	// Already ready, just read it back out
//    	byte[] buffer = new byte[1024];
//        int bytesRead;
//        while ((bytesRead = encoded.read(buffer)) != -1)
//        {
//            decoded.write(buffer, 0, bytesRead);
//        }
        
        IOUtils.copy(encoded, decoded);
        
        return new DecodeResult(parameters);
    }
    
    // Anything past here probably won't be needed

    // reads the APP14 Adobe transform tag
//    private Integer getAdobeTransform(IIOMetadata metadata)
//    {
//        Element tree = (Element)metadata.getAsTree("javax_imageio_jpeg_image_1.0");
//        Element markerSequence = (Element)tree.getElementsByTagName("markerSequence").item(0);
//
//        if (markerSequence.getElementsByTagName("app14Adobe") != null)
//        {
//            Element adobe = (Element)markerSequence.getElementsByTagName("app14Adobe").item(0);
//            return Integer.parseInt(adobe.getAttribute("transform"));
//        }
//        return 0; // Unknown
//    }TODO: PdfBox-Android

    // converts YCCK image to CMYK. YCCK is an equivalent encoding for
    // CMYK data, so no color management code is needed here, nor does the
    // PDF color space have to be consulted
//    private WritableRaster fromYCCKtoCMYK(Raster raster) throws IOException
//    {
//        WritableRaster writableRaster = raster.createCompatibleWritableRaster();
//
//        int[] value = new int[4];
//        for (int y = 0, height = raster.getHeight(); y < height; y++)
//        {
//            for (int x = 0, width = raster.getWidth(); x < width; x++)
//            {
//                raster.getPixel(x, y, value);
//
//                // 4-channels 0..255
//                float Y = value[0];
//                float Cb = value[1];
//                float Cr = value[2];
//                float K = value[3];
//
//                // YCCK to RGB, see http://software.intel.com/en-us/node/442744
//                int r = clamp(Y + 1.402f * Cr - 179.456f);
//                int g = clamp(Y - 0.34414f * Cb - 0.71414f * Cr + 135.45984f);
//                int b = clamp(Y + 1.772f * Cb - 226.816f);
//
//                // naive RGB to CMYK
//                int cyan = 255 - r;
//                int magenta = 255 - g;
//                int yellow = 255 - b;
//
//                // update new raster
//                value[0] = cyan;
//                value[1] = magenta;
//                value[2] = yellow;
//                value[3] = (int)K;
//                writableRaster.setPixel(x, y, value);
//            }
//        }
//        return writableRaster;
//    }TODO: PdfBox-Android

    // converts from BGR to RGB
//    private WritableRaster fromBGRtoRGB(Raster raster) throws IOException
//    {
//        WritableRaster writableRaster = raster.createCompatibleWritableRaster();
//
//        int width = raster.getWidth();
//        int height = raster.getHeight();
//        int w3 = width * 3;
//        int[] tab = new int[w3];
//        //BEWARE: handling the full image at a time is slower than one line at a time        
//        for (int y = 0; y < height; y++)
//        {
//            raster.getPixels(0, y, width, 1, tab);
//            for (int off = 0; off < w3; off += 3)
//            {
//                int tmp = tab[off];
//                tab[off] = tab[off + 2];
//                tab[off + 2] = tmp;
//            }
//            writableRaster.setPixels(0, y, width, 1, tab);
//        }
//        return writableRaster;
//    } TODO: PdfBox-Android

    // clamps value to 0-255 range
    private int clamp(float value)
    {
        return (int)((value < 0) ? 0 : ((value > 255) ? 255 : value));
    }

    @Override
    protected void encode(InputStream input, OutputStream encoded, COSDictionary parameters)
            throws IOException
    {
    	Log.w("PdfBox-Android", "DCTFilter#encode is not implemented yet, skipping this stream.");
    }
}
