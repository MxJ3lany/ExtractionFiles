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
package com.tom_roush.pdfbox.pdmodel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tom_roush.pdfbox.cos.COSArray;
import com.tom_roush.pdfbox.cos.COSBase;
import com.tom_roush.pdfbox.cos.COSDictionary;
import com.tom_roush.pdfbox.cos.COSName;
import com.tom_roush.pdfbox.cos.COSObject;
import com.tom_roush.pdfbox.cos.COSStream;
import com.tom_roush.pdfbox.pdmodel.common.COSArrayList;
import com.tom_roush.pdfbox.pdmodel.common.COSObjectable;
import com.tom_roush.pdfbox.pdmodel.common.PDDestinationOrAction;
import com.tom_roush.pdfbox.pdmodel.common.PDMetadata;
import com.tom_roush.pdfbox.pdmodel.common.PDPageLabels;
import com.tom_roush.pdfbox.pdmodel.documentinterchange.logicalstructure.PDMarkInfo;
import com.tom_roush.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import com.tom_roush.pdfbox.pdmodel.graphics.color.PDOutputIntent;
import com.tom_roush.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentProperties;
import com.tom_roush.pdfbox.pdmodel.interactive.action.PDActionFactory;
import com.tom_roush.pdfbox.pdmodel.interactive.action.PDDocumentCatalogAdditionalActions;
import com.tom_roush.pdfbox.pdmodel.interactive.action.PDURIDictionary;
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDAcroForm;
import com.tom_roush.pdfbox.pdmodel.interactive.pagenavigation.PDThread;
import com.tom_roush.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;

/**
 * The Document Catalog of a PDF.
 *
 * @author Ben Litchfield
 */
public class PDDocumentCatalog implements COSObjectable
{
    private final COSDictionary root;
    private final PDDocument document;
    private PDAcroForm cachedAcroForm;

    /**
     * Constructor. Acroform.
     *
     * @param doc The document that this catalog is part of.
     */
    public PDDocumentCatalog(PDDocument doc)
    {
        document = doc;
        root = new COSDictionary();
        root.setItem(COSName.TYPE, COSName.CATALOG);
        document.getDocument().getTrailer().setItem(COSName.ROOT, root);
    }

    /**
     * Constructor.
     *
     * @param doc The document that this catalog is part of.
     * @param rootDictionary The root dictionary that this object wraps.
     */
    public PDDocumentCatalog(PDDocument doc, COSDictionary rootDictionary)
    {
        document = doc;
        root = rootDictionary;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSDictionary getCOSObject()
    {
        return root;
    }

    /**
     * Get the documents AcroForm. This will return null if no AcroForm is part of the document.
     *
     * @return The documents acroform.
     */
    public PDAcroForm getAcroForm()
    {
        if(cachedAcroForm == null)
        {
            COSDictionary dict = (COSDictionary)root.getDictionaryObject(COSName.ACRO_FORM);
            cachedAcroForm = dict == null ? null : new PDAcroForm(document, dict);
        }
        return cachedAcroForm;
    }

    /**
     * Sets the Acroform for this catalog.
     *
     * @param acroForm The new Acroform.
     */
    public void setAcroForm(PDAcroForm acroForm)
    {
        root.setItem(COSName.ACRO_FORM, acroForm);
        cachedAcroForm = null;
    }

    /**
     * Returns all pages in the document, as a page tree.
     */
    public PDPageTree getPages()
    {
        // TODO cache me?
        return new PDPageTree((COSDictionary) root.getDictionaryObject(COSName.PAGES), document);
    }

    /**
     * Get the viewer preferences associated with this document or null if they do not exist.
     *
     * @return The document's viewer preferences.
     */
    public PDViewerPreferences getViewerPreferences()
    {
        COSBase base = root.getDictionaryObject(COSName.VIEWER_PREFERENCES);
        return base instanceof COSDictionary ? new PDViewerPreferences((COSDictionary) base) : null;
    }

    /**
     * Sets the viewer preferences.
     *
     * @param prefs The new viewer preferences.
     */
    public void setViewerPreferences(PDViewerPreferences prefs)
    {
        root.setItem(COSName.VIEWER_PREFERENCES, prefs);
    }

    /**
     * Get the outline associated with this document or null if it does not exist.
     *
     * @return The document's outline.
     */
    public PDDocumentOutline getDocumentOutline()
    {
        COSDictionary dict = (COSDictionary)root.getDictionaryObject(COSName.OUTLINES);
        return dict == null ? null : new PDDocumentOutline(dict);
    }

    /**
     * Sets the document outlines.
     *
     * @param outlines The new document outlines.
     */
    public void setDocumentOutline(PDDocumentOutline outlines)
    {
        root.setItem(COSName.OUTLINES, outlines);
    }

    /**
     * Returns the document's article threads.
     */
    public List<PDThread> getThreads()
    {
        COSArray array = (COSArray)root.getDictionaryObject(COSName.THREADS);
        if(array == null)
        {
            array = new COSArray();
            root.setItem(COSName.THREADS, array);
        }
        List<PDThread> pdObjects = new ArrayList<PDThread>();
        for(int i = 0; i < array.size(); i++)
        {
            pdObjects.add(new PDThread((COSDictionary)array.getObject(i)));
        }
        return new COSArrayList<PDThread>(pdObjects, array);
    }

    /**
     * Sets the list of threads for this pdf document.
     *
     * @param threads The list of threads, or null to clear it.
     */
    public void setThreads(List threads)
    {
        root.setItem( COSName.THREADS, COSArrayList.converterToCOSArray(threads));
    }

    /**
     * Get the metadata that is part of the document catalog.  This will return null if there is no
     * meta data for this object.
     *
     * @return The metadata for this object.
     */
    public PDMetadata getMetadata()
    {
        COSBase metaObj = root.getDictionaryObject(COSName.METADATA);
        if(metaObj instanceof COSStream)
        {
            return new PDMetadata((COSStream) metaObj);
        }
        return null;
    }

    /**
     * Sets the metadata for this object.  This can be null.
     *
     * @param meta The meta data for this object.
     */
    public void setMetadata(PDMetadata meta)
    {
        root.setItem(COSName.METADATA, meta);
    }

    /**
     * Sets the Document Open Action for this object.
     *
     * @param action The action you want to perform.
     */
    public void setOpenAction(PDDestinationOrAction action)
    {
        root.setItem(COSName.OPEN_ACTION, action);
    }

    /**
     * Get the Document Open Action for this object.
     *
     * @return The action to perform when the document is opened.
     *
     * @throws IOException If there is an error creating the destination or action.
     */
    public PDDestinationOrAction getOpenAction() throws IOException
    {
        COSBase openAction = root.getDictionaryObject(COSName.OPEN_ACTION);

        if(openAction == null)
        {
            return null;
        }
        else if(openAction instanceof COSDictionary)
        {
            return PDActionFactory.createAction((COSDictionary)openAction);
        }
        else if(openAction instanceof COSArray)
        {
            return PDDestination.create(openAction);
        }
        else
        {
            throw new IOException("Unknown OpenAction " + openAction);
        }
    }
    /**
     * @return The Additional Actions for this Document
     */
    public PDDocumentCatalogAdditionalActions getActions()
    {
        COSDictionary addAction = (COSDictionary) root.getDictionaryObject(COSName.AA);
        if (addAction == null)
        {
            addAction = new COSDictionary();
            root.setItem(COSName.AA, addAction);
        }
        return new PDDocumentCatalogAdditionalActions(addAction);
    }

    /**
     * Sets the additional actions for the document.
     *
     * @param actions The actions that are associated with this document.
     */
    public void setActions(PDDocumentCatalogAdditionalActions actions)
    {
        root.setItem(COSName.AA, actions);
    }

    /**
     * @return The names dictionary for this document or null if none exist.
     */
    public PDDocumentNameDictionary getNames()
    {
        COSDictionary names = (COSDictionary) root.getDictionaryObject(COSName.NAMES);
        return names == null ? null : new PDDocumentNameDictionary(this, names);
    }

    /**
     * @return The named destinations dictionary for this document or null if none exists.
     */
    public PDDocumentNameDestinationDictionary getDests()
    {
        PDDocumentNameDestinationDictionary nameDic = null;
        COSDictionary dests = (COSDictionary) root.getDictionaryObject(COSName.DESTS);
        if (dests != null)
        {
            nameDic = new PDDocumentNameDestinationDictionary(dests);
        }
        return nameDic;
    }

    /**
     * Sets the names dictionary for the document.
     *
     * @param names The names dictionary that is associated with this document.
     */
    public void setNames(PDDocumentNameDictionary names)
    {
        root.setItem(COSName.NAMES, names);
    }

    /**
     * Get info about doc's usage of tagged features.  This will return null if there is no
     * information.
     *
     * @return The new mark info.
     */
    public PDMarkInfo getMarkInfo()
    {
        COSDictionary dic = (COSDictionary)root.getDictionaryObject(COSName.MARK_INFO);
        return dic == null ? null : new PDMarkInfo(dic);
    }

    /**
     * Sets information about the doc's usage of tagged features.
     *
     * @param markInfo The new MarkInfo data.
     */
    public void setMarkInfo(PDMarkInfo markInfo)
    {
        root.setItem(COSName.MARK_INFO, markInfo);
    }

    /**
     * Get the list of OutputIntents defined in the document.
     *
     * @return The list of PDOutputIntent
     */
    public List<PDOutputIntent> getOutputIntents ()
    {
        List<PDOutputIntent> retval = new ArrayList<PDOutputIntent>();
        COSArray array = (COSArray)root.getDictionaryObject(COSName.OUTPUT_INTENTS);
        if (array != null)
        {
            for (COSBase cosBase : array)
            {
                if (cosBase instanceof COSObject)
                {
                    cosBase = ((COSObject)cosBase).getObject();
                }
                PDOutputIntent oi = new PDOutputIntent((COSDictionary) cosBase);
                retval.add(oi);
            }
        }
        return retval;
    }

    /**
     * Add an OutputIntent to the list. If there is not OutputIntent, the list is created and the
     * first element added.
     *
     * @param outputIntent the OutputIntent to add.
     */
    public void addOutputIntent(PDOutputIntent outputIntent)
    {
        COSArray array = (COSArray)root.getDictionaryObject(COSName.OUTPUT_INTENTS);
        if (array == null)
        {
            array = new COSArray();
            root.setItem(COSName.OUTPUT_INTENTS, array);
        }
        array.add(outputIntent.getCOSObject());
    }

    /**
     * Replace the list of OutputIntents of the document.
     *
     * @param outputIntents the list of OutputIntents, if the list is empty all OutputIntents are
     * removed.
     */
    public void setOutputIntents(List<PDOutputIntent> outputIntents)
    {
        COSArray array = new COSArray();
        for (PDOutputIntent intent : outputIntents)
        {
            array.add(intent.getCOSObject());
        }
        root.setItem(COSName.OUTPUT_INTENTS, array);
    }

    /**
     * Returns the page display mode.
     */
    public PageMode getPageMode()
    {
        String mode = root.getNameAsString(COSName.PAGE_MODE);
        if (mode != null)
        {
            return PageMode.fromString(mode);
        }
        else
        {
            return PageMode.USE_NONE;
        }
    }

    /**
     * Sets the page mode.
     *
     * @param mode The new page mode.
     */
    public void setPageMode(PageMode mode)
    {
        root.setName(COSName.PAGE_MODE, mode.stringValue());
    }

    /**
     * Returns the page layout.
     */
    public PageLayout getPageLayout()
    {
        String mode = root.getNameAsString(COSName.PAGE_LAYOUT);
        if (mode != null)
        {
            return PageLayout.fromString(mode);
        }
        else
        {
            return PageLayout.SINGLE_PAGE;
        }
    }

    /**
     * Sets the page layout.
     *
     * @param layout The new page layout.
     */
    public void setPageLayout(PageLayout layout)
    {
        root.setName(COSName.PAGE_LAYOUT, layout.stringValue());
    }

    /**
     * Returns the document-level URI.
     */
    public PDURIDictionary getURI()
    {
        COSDictionary uri = (COSDictionary)root.getDictionaryObject(COSName.URI);
        return uri == null ? null : new PDURIDictionary(uri);
    }

    /**
     * Sets the document level uri.
     *
     * @param uri The new document level uri.
     */
    public void setURI(PDURIDictionary uri)
    {
        root.setItem(COSName.URI, uri);
    }

    /**
     * Get the document's structure tree root, or null if none exists.
     */
    public PDStructureTreeRoot getStructureTreeRoot()
    {
        COSDictionary dict = (COSDictionary)root.getDictionaryObject(COSName.STRUCT_TREE_ROOT);
        return dict == null ? null : new PDStructureTreeRoot(dict);
    }

    /**
     * Sets the document's structure tree root.
     *
     * @param treeRoot The new structure tree.
     */
    public void setStructureTreeRoot(PDStructureTreeRoot treeRoot)
    {
        root.setItem(COSName.STRUCT_TREE_ROOT, treeRoot);
    }

    /**
     * Returns the language for the document, or null.
     */
    public String getLanguage()
    {
        return root.getString(COSName.LANG);
    }

    /**
     * Sets the Language for the document.
     *
     * @param language The new document language.
     */
    public void setLanguage(String language)
    {
        root.setString(COSName.LANG, language);
    }

    /**
     * Returns the PDF specification version this document conforms to.
     *
     * @return the PDF version (e.g. "1.4")
     */
    public String getVersion()
    {
        return root.getNameAsString(COSName.VERSION);
    }

    /**
     * Sets the PDF specification version this document conforms to.
     *
     * @param version the PDF version (e.g. "1.4")
     */
    public void setVersion(String version)
    {
        root.setName(COSName.VERSION, version);
    }

    /**
     * Returns the page labels descriptor of the document.
     *
     * @return the page labels descriptor of the document.
     * @throws IOException If there is a problem retrieving the page labels.
     */
    public PDPageLabels getPageLabels() throws IOException
    {
        COSDictionary dict = (COSDictionary) root.getDictionaryObject(COSName.PAGE_LABELS);
        return dict == null ? null : new PDPageLabels(document, dict);
    }

    /**
     * Sets the page label descriptor for the document.
     *
     * @param labels the new page label descriptor to set.
     */
    public void setPageLabels(PDPageLabels labels)
    {
        root.setItem(COSName.PAGE_LABELS, labels);
    }

    /**
     * Get the optional content properties dictionary associated with this document.
     *
     * @return the optional properties dictionary or null if it is not present
     * @since PDF 1.5
     */
    public PDOptionalContentProperties getOCProperties()
    {
        COSDictionary dict = (COSDictionary)root.getDictionaryObject(COSName.OCPROPERTIES);
        return dict == null ? null : new PDOptionalContentProperties(dict);
    }

    /**
     * Sets the optional content properties dictionary.
     *
     * @param ocProperties the optional properties dictionary
     * @since PDF 1.5
     */
    public void setOCProperties(PDOptionalContentProperties ocProperties)
    {
        root.setItem(COSName.OCPROPERTIES, ocProperties);

        // optional content groups require PDF 1.5
        if (ocProperties != null && document.getVersion() < 1.5)
        {
            document.setVersion(1.5f);
        }
    }
}
