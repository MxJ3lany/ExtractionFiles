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
package com.tom_roush.fontbox.ttf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * TrueType font file parser.
 * 
 * @author Ben Litchfield
 */
public class TTFParser
{
    private boolean isEmbedded = false;
    private boolean parseOnDemandOnly = false;

    /**
     * Constructor.
     */
    public TTFParser()
    {
        this(false);
    }

    /**
     * Constructor.
     *  
     * @param isEmbedded true if the font is embedded in PDF
     */
    public TTFParser(boolean isEmbedded)
    {
        this(isEmbedded, false);
    }

    /**
     *  Constructor.
     *  
     * @param isEmbedded true if the font is embedded in PDF
     * @param parseOnDemand true if the tables of the font should be parsed on demand
     */
    public TTFParser(boolean isEmbedded, boolean parseOnDemand)
    {
        this.isEmbedded = isEmbedded;
        parseOnDemandOnly = parseOnDemand;
    }

    /**
     * Parse a file and get a true type font.
     *
     * @param ttfFile The TTF file.
     * @return A true type font.
     * @throws IOException If there is an error parsing the true type font.
     */
    public TrueTypeFont parse(String ttfFile) throws IOException
    {
        return parse(new File(ttfFile));
    }

    /**
     * Parse a file and get a true type font.
     *
     * @param ttfFile The TTF file.
     * @return A true type font.
     * @throws IOException If there is an error parsing the true type font.
     */
    public TrueTypeFont parse(File ttfFile) throws IOException
    {
        return parse(new RAFDataStream(ttfFile, "r"));
    }

    /**
     * Parse a file and get a true type font.
     *
     * @param ttfData The TTF data to parse.
     * @return A true type font.
     * @throws IOException If there is an error parsing the true type font.
     */
    public TrueTypeFont parse(InputStream ttfData) throws IOException
    {
        return parse(new MemoryTTFDataStream(ttfData));
    }

    /**
     Parse a file and get a true type font.

     * @param ttfData The TTF data to parse.
     * @return A true type font.
     * @throws IOException If there is an error parsing the true type font.
     */
    public TrueTypeFont parseEmbedded(InputStream ttfData) throws IOException
    {
    	this.isEmbedded = true;
    	return parse(new MemoryTTFDataStream(ttfData));
    }

    /**
     * Parse a file and get a true type font.
     *
     * @param raf The TTF file.
     * @return A true type font.
     * @throws IOException If there is an error parsing the true type font.
     */
    TrueTypeFont parse(TTFDataStream raf) throws IOException
    {
        TrueTypeFont font = newFont(raf);
        font.setVersion(raf.read32Fixed());
        int numberOfTables = raf.readUnsignedShort();
        int searchRange = raf.readUnsignedShort();
        int entrySelector = raf.readUnsignedShort();
        int rangeShift = raf.readUnsignedShort();
        for (int i = 0; i < numberOfTables; i++)
        {
            TTFTable table = readTableDirectory(font, raf);
            font.addTable(table);
        }
        // parse tables if wanted
        if (!parseOnDemandOnly)
        {
            parseTables(font, raf);
        }

        return font;
    }

    TrueTypeFont newFont(TTFDataStream raf)
    {
        return new TrueTypeFont(raf);
    }

    /**
     * Parse all tables and check if all needed tables are present.
     *
     * @param font the TrueTypeFont instance holding the parsed data.
     * @param raf the data stream of the to be parsed ttf font
     * @throws IOException If there is an error parsing the true type font.
     */
    private void parseTables(TrueTypeFont font, TTFDataStream raf) throws IOException
    {
        for (TTFTable table : font.getTables())
        {
            if (!table.getInitialized())
            {
                font.readTable(table);
            }
        }

        HeaderTable head = font.getHeader();
        if (head == null)
        {
            throw new IOException("head is mandatory");
        }

        HorizontalHeaderTable hh = font.getHorizontalHeader();
        if (hh == null)
        {
            throw new IOException("hhead is mandatory");
        }

        MaximumProfileTable maxp = font.getMaximumProfile();
        if (maxp == null)
        {
            throw new IOException("maxp is mandatory");
        }

        PostScriptTable post = font.getPostScript();
        if (post == null && !isEmbedded)
        {
            // in an embedded font this table is optional
            throw new IOException("post is mandatory");
        }

        IndexToLocationTable loc = font.getIndexToLocation();
        if (loc == null)
        {
            throw new IOException("loca is mandatory");
        }
        // check other mandatory tables
        if (font.getGlyph() == null)
        {
            throw new IOException("glyf is mandatory");
        }
        if (font.getNaming() == null && !isEmbedded)
        {
            throw new IOException("name is mandatory");
        }
        if (font.getHorizontalMetrics() == null)
        {
            throw new IOException("hmtx is mandatory");
        }

        // check others mandatory tables
        if (!isEmbedded && font.getCmap() == null)
        {
            throw new IOException("cmap is mandatory");
        }
    }

    private TTFTable readTableDirectory(TrueTypeFont font, TTFDataStream raf) throws IOException
    {
        TTFTable table = null;
        String tag = raf.readString(4);
        if (tag.equals(CmapTable.TAG))
        {
            table = new CmapTable(font);
        }
        else if (tag.equals(GlyphTable.TAG))
        {
            table = new GlyphTable(font);
        }
        else if (tag.equals(HeaderTable.TAG))
        {
            table = new HeaderTable(font);
        }
        else if (tag.equals(HorizontalHeaderTable.TAG))
        {
            table = new HorizontalHeaderTable(font);
        }
        else if (tag.equals(HorizontalMetricsTable.TAG))
        {
            table = new HorizontalMetricsTable(font);
        }
        else if (tag.equals(IndexToLocationTable.TAG))
        {
            table = new IndexToLocationTable(font);
        }
        else if (tag.equals(MaximumProfileTable.TAG))
        {
            table = new MaximumProfileTable(font);
        }
        else if (tag.equals(NamingTable.TAG))
        {
            table = new NamingTable(font);
        }
        else if (tag.equals(OS2WindowsMetricsTable.TAG))
        {
            table = new OS2WindowsMetricsTable(font);
        }
        else if (tag.equals(PostScriptTable.TAG))
        {
            table = new PostScriptTable(font);
        }
        else if (tag.equals(DigitalSignatureTable.TAG))
        {
            table = new DigitalSignatureTable(font);
        }
        else if (tag.equals(KerningTable.TAG))
        {
            table = new KerningTable(font);
        }
        else if (tag.equals(VerticalHeaderTable.TAG))
        {
            table = new VerticalHeaderTable(font);
        }
        else if (tag.equals(VerticalMetricsTable.TAG))
        {
            table = new VerticalMetricsTable(font);
        }
        else if (tag.equals(VerticalOriginTable.TAG))
        {
            table = new VerticalOriginTable(font);
        }
        else
        {
            table = readTable(font, tag);
        }
        table.setTag(tag);
        table.setCheckSum(raf.readUnsignedInt());
        table.setOffset(raf.readUnsignedInt());
        table.setLength(raf.readUnsignedInt());
        return table;
    }

    protected TTFTable readTable(TrueTypeFont font, String tag)
    {
        // unknown table type but read it anyway.
        return new TTFTable(font);
    }
}
