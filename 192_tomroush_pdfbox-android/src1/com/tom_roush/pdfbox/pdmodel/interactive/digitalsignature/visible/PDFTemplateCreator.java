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
package com.tom_roush.pdfbox.pdmodel.interactive.digitalsignature.visible;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.tom_roush.harmony.awt.geom.AffineTransform;
import com.tom_roush.pdfbox.cos.COSName;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDResources;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.common.PDStream;
import com.tom_roush.pdfbox.pdmodel.graphics.form.PDFormXObject;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDAcroForm;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDSignatureField;
/**
 * Using that class, we build pdf template.
 * @author Vakhtang Koroghlishvili
 */
public class PDFTemplateCreator
{
    PDFTemplateBuilder pdfBuilder;

    /**
     * sets PDFBuilder
     * 
     * @param bookBuilder
     */
    public PDFTemplateCreator(PDFTemplateBuilder bookBuilder)
    {
        pdfBuilder = bookBuilder;
    }

    /**
     * that method returns object of PDFStructure
     * 
     * @return PDFStructure
     */
    public PDFTemplateStructure getPdfStructure()
    {
        return pdfBuilder.getStructure();
    }

    /**
     * this method builds pdf  step by step, and finally it returns stream of visible signature
     * @param properties
     * @return InputStream
     * @throws IOException
     */
    public InputStream buildPDF(PDVisibleSignDesigner properties) throws IOException
    {
        Log.i("PdfBox-Android", "pdf building has been started");
        PDFTemplateStructure pdfStructure = pdfBuilder.getStructure();

        // we create array of [Text, ImageB, ImageC, ImageI]
        pdfBuilder.createProcSetArray();
        
        //create page
        pdfBuilder.createPage(properties);
        PDPage page = pdfStructure.getPage();

        //create template
        pdfBuilder.createTemplate(page);
        PDDocument template = pdfStructure.getTemplate();
        
        //create /AcroForm
        pdfBuilder.createAcroForm(template);
        PDAcroForm acroForm = pdfStructure.getAcroForm();

        // AcroForm contains singature fields
        pdfBuilder.createSignatureField(acroForm);
        PDSignatureField pdSignatureField = pdfStructure.getSignatureField();
        
        // create signature
        pdfBuilder.createSignature(pdSignatureField, page, properties.getSignatureFieldName());
       
        // that is /AcroForm/DR entry
        pdfBuilder.createAcroFormDictionary(acroForm, pdSignatureField);
        
        // create AffineTransform
        pdfBuilder.createAffineTransform(properties.getAffineTransformParams());
        AffineTransform transform = pdfStructure.getAffineTransform();
       
        // rectangle, formatter, image. /AcroForm/DR/XObject contains that form
        pdfBuilder.createSignatureRectangle(pdSignatureField, properties);
        pdfBuilder.createFormaterRectangle(properties.getFormaterRectangleParams());
        PDRectangle formater = pdfStructure.getFormaterRectangle();
        pdfBuilder.createSignatureImage(template, properties.getImage());

        // create form stream, form and  resource. 
        pdfBuilder.createHolderFormStream(template);
        PDStream holderFormStream = pdfStructure.getHolderFormStream();
        pdfBuilder.createHolderFormResources();
        PDResources holderFormResources = pdfStructure.getHolderFormResources();
        pdfBuilder.createHolderForm(holderFormResources, holderFormStream, formater);
       
        // that is /AP entry the appearance dictionary.
        pdfBuilder.createAppearanceDictionary(pdfStructure.getHolderForm(), pdSignatureField);
        
        // inner form stream, form and resource (hlder form containts inner form)
        pdfBuilder.createInnerFormStream(template);
        pdfBuilder.createInnerFormResource();
        PDResources innerFormResource = pdfStructure.getInnerFormResources();
        pdfBuilder.createInnerForm(innerFormResource, pdfStructure.getInnterFormStream(), formater);
       PDFormXObject innerForm = pdfStructure.getInnerForm();
       
        // inner form must be in the holder form as we wrote
        pdfBuilder.insertInnerFormToHolerResources(innerForm, holderFormResources);
        
        //  Image form is in this structure: /AcroForm/DR/FRM0/Resources/XObject/n0
        pdfBuilder.createImageFormStream(template);
        PDStream imageFormStream = pdfStructure.getImageFormStream();
        pdfBuilder.createImageFormResources();
        PDResources imageFormResources = pdfStructure.getImageFormResources();
        pdfBuilder.createImageForm(imageFormResources, innerFormResource, imageFormStream, formater,
                transform, pdfStructure.getImage());
       
        // now inject procSetArray
        pdfBuilder.injectProcSetArray(innerForm, page, innerFormResource, imageFormResources,
                holderFormResources, pdfStructure.getProcSet());

        COSName imgFormName = pdfStructure.getImageFormName();
        COSName imgName = pdfStructure.getImageName();
        COSName innerFormName = pdfStructure.getInnerFormName();

       // now create Streams of AP
        pdfBuilder.injectAppearanceStreams(holderFormStream, imageFormStream, imageFormStream,
                imgFormName, imgName, innerFormName, properties);
        pdfBuilder.createVisualSignature(template);
        pdfBuilder.createWidgetDictionary(pdSignatureField, holderFormResources);
        
        ByteArrayInputStream in = pdfStructure.getTemplateAppearanceStream();
        Log.i("PdfBox-Android", "stream returning started, size= " + in.available());
        
        // we must close the document
        template.close();
        
        // return result of the stream 
        return in;
    }
}
