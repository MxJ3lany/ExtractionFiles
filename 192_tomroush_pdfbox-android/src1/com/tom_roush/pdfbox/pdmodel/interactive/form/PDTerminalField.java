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
package com.tom_roush.pdfbox.pdmodel.interactive.form;

import com.tom_roush.pdfbox.cos.COSArray;
import com.tom_roush.pdfbox.cos.COSBase;
import com.tom_roush.pdfbox.cos.COSDictionary;
import com.tom_roush.pdfbox.cos.COSInteger;
import com.tom_roush.pdfbox.cos.COSName;
import com.tom_roush.pdfbox.pdmodel.common.COSArrayList;
import com.tom_roush.pdfbox.pdmodel.fdf.FDFField;
import com.tom_roush.pdfbox.pdmodel.interactive.action.PDFormFieldAdditionalActions;
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A field in an interactive form.
 * Fields may be one of four types: button, text, choice, or signature.
 *
 * @author sug
 */
public abstract class PDTerminalField extends PDField
{
    /**
     * Constructor.
     *
     * @param acroForm The form that this field is part of.
     */
    protected PDTerminalField(PDAcroForm acroForm)
    {
        super(acroForm);
    }

    /**
     * Constructor.
     *
     * @param acroForm The form that this field is part of.
     * @param field the PDF object to represent as a field.
     * @param parent the parent node of the node
     */
    PDTerminalField(PDAcroForm acroForm, COSDictionary field, PDNonTerminalField parent)
    {
        super(acroForm, field, parent);
    }

    /**
     * Set the actions of the field.
     *
     * @param actions The field actions.
     */
    public void setActions(PDFormFieldAdditionalActions actions)
    {
        dictionary.setItem(COSName.AA, actions);
    }

    @Override
    public int getFieldFlags()
    {
        int retval = 0;
        COSInteger ff = (COSInteger) dictionary.getDictionaryObject(COSName.FF);
        if (ff != null)
        {
            retval = ff.intValue();
        }
        else if (parent != null)
        {
            retval = parent.getFieldFlags();
        }
        return retval;
    }

    @Override
    public String getFieldType()
    {
        String fieldType = dictionary.getNameAsString(COSName.FT);
        if (fieldType == null && parent != null)
        {
            fieldType = parent.getFieldType();
        }
        return fieldType;
    }

    @Override
    public void importFDF(FDFField fdfField) throws IOException
    {
        super.importFDF(fdfField);

        PDAnnotationWidget widget = getWidgets().get(0); // fixme: ignores multiple widgets
        if (widget != null)
        {
            int annotFlags = widget.getAnnotationFlags();
            Integer f = fdfField.getWidgetFieldFlags();
            if (f != null)
            {
                widget.setAnnotationFlags(f);
            }
            else
            {
                // these are suppose to be ignored if the F is set.
                Integer setF = fdfField.getSetWidgetFieldFlags();
                if (setF != null)
                {
                    annotFlags = annotFlags | setF;
                    widget.setAnnotationFlags(annotFlags);
                }

                Integer clrF = fdfField.getClearWidgetFieldFlags();
                if (clrF != null)
                {
                    // we have to clear the bits of the document fields for every bit that is
                    // set in this field.
                    //
                    // Example:
                    // docF = 1011
                    // clrF = 1101
                    // clrFValue = 0010;
                    // newValue = 1011 & 0010 which is 0010
                    int clrFValue = clrF;
                    clrFValue ^= 0xFFFFFFFFL;
                    annotFlags = annotFlags & clrFValue;
                    widget.setAnnotationFlags(annotFlags);
                }
            }
        }
    }

    @Override
    FDFField exportFDF() throws IOException
    {
        FDFField fdfField = new FDFField();
        fdfField.setPartialFieldName(getPartialName());
        fdfField.setValue(dictionary.getDictionaryObject(COSName.V));

        // fixme: the old code which was here assumed that Kids were PDField instances,
        //        which is never true. They're annotation widgets.

        return fdfField;
    }

    /**
     * Returns the widget annotations associated with this field.
     *
     * @return The list of widget annotations.
     */
    public List<PDAnnotationWidget> getWidgets()
    {
        List<PDAnnotationWidget> widgets = new ArrayList<PDAnnotationWidget>();
        COSArray kids = (COSArray) dictionary.getDictionaryObject(COSName.KIDS);
        if (kids == null)
        {
            // the field itself is a widget
            widgets.add(new PDAnnotationWidget(dictionary));
        }
        else if (kids.size() > 0)
        {
            // there are multiple widgets
            for (int i = 0; i < kids.size(); i++)
            {
                COSBase kid = kids.getObject(i);
                if (kid instanceof COSDictionary)
                {
                    widgets.add(new PDAnnotationWidget((COSDictionary) kid));
                }
            }
        }
        return widgets;
    }

    /**
     * Sets the field's widget annotations.
     *
     * @param children The list of widget annotations.
     */
    public void setWidgets(List<PDAnnotationWidget> children)
    {
        COSArray kidsArray = COSArrayList.converterToCOSArray(children);
        dictionary.setItem(COSName.KIDS, kidsArray);
    }

    /**
     * This will get the single associated widget that is part of this field. This occurs when the
     * Widget is embedded in the fields dictionary. Sometimes there are multiple sub widgets
     * associated with this field, in which case you want to use getWidgets(). If the kids entry is
     * specified, then the first entry in that list will be returned.
     *
     * @return The widget that is associated with this field.
     * @deprecated Fields may have more than one widget, call {@link #getWidgets()} instead.
     */
    @Deprecated
    public PDAnnotationWidget getWidget()
    {
        return getWidgets().get(0);
    }

    /**
     * Applies a value change to the field. Generates appearances if required and raises events.
     *
     * @throws IOException if the appearance couldn't be generated
     */
    protected final void applyChange() throws IOException
    {
        if(!acroForm.getNeedAppearances())
        {
            constructAppearances();
        }
    }
    // if we supported JavaScript we would raise a field changed event here

    /**
     * Constructs appearance streams and appearance dictionaries for all widget annotations.
     * Subclasses should not call this method directly but via {@link #applyChange()}.
     *
     * @throws IOException if the appearance couldn't be generated
     */
    abstract void constructAppearances() throws IOException;
}
