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
package com.tom_roush.fontbox.cff;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class represents a parser for a CFF font. 
 * @author Villu Ruusmann
 */
public class CFFParser
{
    private static final String TAG_OTTO = "OTTO";
    private static final String TAG_TTCF = "ttcf";
    private static final String TAG_TTFONLY = "\u0000\u0001\u0000\u0000";

    private CFFDataInput input = null;
    private Header header = null;
    private IndexData nameIndex = null;
    private IndexData topDictIndex = null;
    private IndexData stringIndex = null;

    // for debugging only
    private String debugFontName;

    /**
     * Parsing CFF Font using a byte array as input.
     * @param bytes the given byte array
     * @return the parsed CFF fonts
     * @throws IOException If there is an error reading from the stream
     */
    public List<CFFFont> parse(byte[] bytes) throws IOException
    {
        input = new CFFDataInput(bytes);

        String firstTag = readTagName(input);
        // try to determine which kind of font we have
        if (TAG_OTTO.equals(firstTag))
        {
            // this is OpenType font containing CFF data
            // so find CFF tag
            short numTables = input.readShort();
            short searchRange = input.readShort();
            short entrySelector = input.readShort();
            short rangeShift = input.readShort();

            boolean cffFound = false;
            for (int q = 0; q < numTables; q++)
            {
                String tagName = readTagName(input);
                long checksum = readLong(input);
                long offset = readLong(input);
                long length = readLong(input);
                if (tagName.equals("CFF "))
                {
                    cffFound = true;
                    byte[] bytes2 = new byte[(int) length];
                    System.arraycopy(bytes, (int) offset, bytes2, 0, bytes2.length);
                    input = new CFFDataInput(bytes2);
                    break;
                }
            }
            if (!cffFound)
            {
                throw new IOException("CFF tag not found in this OpenType font.");
            }
        }
        else if (TAG_TTCF.equals(firstTag))
        {
            throw new IOException("True Type Collection fonts are not supported.");
        }
        else if (TAG_TTFONLY.equals(firstTag))
        {
            throw new IOException("OpenType fonts containing a true type font are not supported.");
        }
        else
        {
            input.setPosition(0);
        }

        header = readHeader(input);
        nameIndex = readIndexData(input);
        topDictIndex = readIndexData(input);
        stringIndex = readIndexData(input);
        IndexData globalSubrIndex = readIndexData(input);

        List<CFFFont> fonts = new ArrayList<CFFFont>();
        for (int i = 0; i < nameIndex.getCount(); i++)
        {
            CFFFont font = parseFont(i);
            font.setGlobalSubrIndex(globalSubrIndex);
            font.setData(bytes);
            fonts.add(font);
        }
        return fonts;
    }

    private static String readTagName(CFFDataInput input) throws IOException
    {
        byte[] b = input.readBytes(4);
        return new String(b, "ISO-8859-1");
    }

    private static long readLong(CFFDataInput input) throws IOException
    {
        return (input.readCard16() << 16) | input.readCard16();
    }

    private static Header readHeader(CFFDataInput input) throws IOException
    {
        Header cffHeader = new Header();
        cffHeader.major = input.readCard8();
        cffHeader.minor = input.readCard8();
        cffHeader.hdrSize = input.readCard8();
        cffHeader.offSize = input.readOffSize();
        return cffHeader;
    }

    private static IndexData readIndexData(CFFDataInput input) throws IOException
    {
        int count = input.readCard16();
        IndexData index = new IndexData(count);
        if (count == 0)
        {
            return index;
        }
        int offSize = input.readOffSize();
        for (int i = 0; i <= count; i++)
        {
        	int offset = input.readOffset(offSize);
        	if (offset > input.length())
        	{
        		throw new IOException("illegal offset value " + offset + " in CFF font");
        	}
        	index.setOffset(i, offset);
        }
        int dataSize = index.getOffset(count) - index.getOffset(0);
        index.initData(dataSize);
        for (int i = 0; i < dataSize; i++)
        {
            index.setData(i, input.readCard8());
        }
        return index;
    }

    private static DictData readDictData(CFFDataInput input) throws IOException
    {
        DictData dict = new DictData();
        dict.entries = new ArrayList<DictData.Entry>();
        while (input.hasRemaining())
        {
            DictData.Entry entry = readEntry(input);
            dict.entries.add(entry);
        }
        return dict;
    }

    private static DictData.Entry readEntry(CFFDataInput input) throws IOException
    {
        DictData.Entry entry = new DictData.Entry();
        while (true)
        {
            int b0 = input.readUnsignedByte();

            if (b0 >= 0 && b0 <= 21)
            {
                entry.operator = readOperator(input, b0);
                break;
            }
            else if (b0 == 28 || b0 == 29)
            {
                entry.operands.add(readIntegerNumber(input, b0));
            }
            else if (b0 == 30)
            {
                entry.operands.add(readRealNumber(input, b0));
            }
            else if (b0 >= 32 && b0 <= 254)
            {
                entry.operands.add(readIntegerNumber(input, b0));
            }
            else
            {
                throw new IllegalArgumentException();
            }
        }
        return entry;
    }

    private static CFFOperator readOperator(CFFDataInput input, int b0) throws IOException
    {
        CFFOperator.Key key = readOperatorKey(input, b0);
        return CFFOperator.getOperator(key);
    }

    private static CFFOperator.Key readOperatorKey(CFFDataInput input, int b0) throws IOException
    {
        if (b0 == 12)
        {
            int b1 = input.readUnsignedByte();
            return new CFFOperator.Key(b0, b1);
        }
        return new CFFOperator.Key(b0);
    }

    private static Integer readIntegerNumber(CFFDataInput input, int b0) throws IOException
    {
        if (b0 == 28)
        {
            int b1 = input.readUnsignedByte();
            int b2 = input.readUnsignedByte();
            return (int) (short) (b1 << 8 | b2);
        }
        else if (b0 == 29)
        {
            int b1 = input.readUnsignedByte();
            int b2 = input.readUnsignedByte();
            int b3 = input.readUnsignedByte();
            int b4 = input.readUnsignedByte();
            return b1 << 24 | b2 << 16 | b3 << 8 | b4;
        }
        else if (b0 >= 32 && b0 <= 246)
        {
            return b0 - 139;
        }
        else if (b0 >= 247 && b0 <= 250)
        {
            int b1 = input.readUnsignedByte();
            return (b0 - 247) * 256 + b1 + 108;
        }
        else if (b0 >= 251 && b0 <= 254)
        {
            int b1 = input.readUnsignedByte();
            return -(b0 - 251) * 256 - b1 - 108;
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * @param b0  
     */
    private static Double readRealNumber(CFFDataInput input, int b0) throws IOException
    {
        StringBuffer sb = new StringBuffer();
        boolean done = false;
        boolean exponentMissing = false;
        while (!done)
        {
            int b = input.readUnsignedByte();
            int[] nibbles = { b / 16, b % 16 };
            for (int nibble : nibbles)
            {
                switch (nibble)
                {
                case 0x0:
                case 0x1:
                case 0x2:
                case 0x3:
                case 0x4:
                case 0x5:
                case 0x6:
                case 0x7:
                case 0x8:
                case 0x9:
                    sb.append(nibble);
                    exponentMissing = false;
                    break;
                case 0xa:
                    sb.append(".");
                    break;
                case 0xb:
                    sb.append("E");
                    exponentMissing = true;
                    break;
                case 0xc:
                    sb.append("E-");
                    exponentMissing = true;
                    break;
                case 0xd:
                    break;
                case 0xe:
                    sb.append("-");
                    break;
                case 0xf:
                    done = true;
                    break;
                default:
                    throw new IllegalArgumentException();
                }
            }
        }
        if (exponentMissing)
        {
            // the exponent is missing, just append "0" to avoid an exception
            // not sure if 0 is the correct value, but it seems to fit
            // see PDFBOX-1522
            sb.append("0");
        }
        return Double.valueOf(sb.toString());
    }

    private CFFFont parseFont(int index) throws IOException
    {
        // name index
        DataInput nameInput = new DataInput(nameIndex.getBytes(index));
        String name = nameInput.getString();

        // top dict
        CFFDataInput topDictInput = new CFFDataInput(topDictIndex.getBytes(index));
        DictData topDict = readDictData(topDictInput);

        // we dont't support synthetic fonts
        DictData.Entry syntheticBaseEntry = topDict.getEntry("SyntheticBase");
        if (syntheticBaseEntry != null)
        {
            throw new IOException("Synthetic Fonts are not supported");
        }

        // determine if this is a Type 1-equivalent font or a CIDFont
        CFFFont font;
        boolean isCIDFont = topDict.getEntry("ROS") != null;
        if (isCIDFont)
        {
            font = new CFFCIDFont();
            DictData.Entry rosEntry = topDict.getEntry("ROS");
            ((CFFCIDFont) font).setRegistry(readString(rosEntry.getNumber(0).intValue()));
            ((CFFCIDFont) font).setOrdering(readString(rosEntry.getNumber(1).intValue()));
            ((CFFCIDFont) font).setSupplement(rosEntry.getNumber(2).intValue());
        }
        else
        {
            font = new CFFType1Font();
        }

        // name
        debugFontName = name;
        font.setName(name);

        // top dict
        font.addValueToTopDict("version", getString(topDict, "version"));
        font.addValueToTopDict("Notice", getString(topDict, "Notice"));
        font.addValueToTopDict("Copyright", getString(topDict, "Copyright"));
        font.addValueToTopDict("FullName", getString(topDict, "FullName"));
        font.addValueToTopDict("FamilyName", getString(topDict, "FamilyName"));
        font.addValueToTopDict("Weight", getString(topDict, "Weight"));
        font.addValueToTopDict("isFixedPitch", getBoolean(topDict, "isFixedPitch", false));
        font.addValueToTopDict("ItalicAngle", getNumber(topDict, "ItalicAngle", 0));
        font.addValueToTopDict("UnderlinePosition", getNumber(topDict, "UnderlinePosition", -100));
        font.addValueToTopDict("UnderlineThickness", getNumber(topDict, "UnderlineThickness", 50));
        font.addValueToTopDict("PaintType", getNumber(topDict, "PaintType", 0));
        font.addValueToTopDict("CharstringType", getNumber(topDict, "CharstringType", 2));
        font.addValueToTopDict("FontMatrix", getArray(topDict, "FontMatrix", Arrays.<Number>asList(
                                                      0.001, (double) 0, (double) 0, 0.001,
                                                      (double) 0, (double) 0)));
        font.addValueToTopDict("UniqueID", getNumber(topDict, "UniqueID", null));
        font.addValueToTopDict("FontBBox", getArray(topDict, "FontBBox",
                                                    Arrays.<Number> asList(0, 0, 0, 0)));
        font.addValueToTopDict("StrokeWidth", getNumber(topDict, "StrokeWidth", 0));
        font.addValueToTopDict("XUID", getArray(topDict, "XUID", null));

        // charstrings index
        DictData.Entry charStringsEntry = topDict.getEntry("CharStrings");
        int charStringsOffset = charStringsEntry.getNumber(0).intValue();
        input.setPosition(charStringsOffset);
        IndexData charStringsIndex = readIndexData(input);

        // charset
        DictData.Entry charsetEntry = topDict.getEntry("charset");
        CFFCharset charset;
        if (charsetEntry != null)
        {
        	int charsetId = charsetEntry.getNumber(0).intValue();
        	if (!isCIDFont && charsetId == 0)
        	{
        		charset = CFFISOAdobeCharset.getInstance();
        	}
        	else if (!isCIDFont && charsetId == 1)
        	{
        		charset = CFFExpertCharset.getInstance();
        	}
        	else if (!isCIDFont && charsetId == 2)
        	{
        		charset = CFFExpertSubsetCharset.getInstance();
        	}
        	else
        	{
        		input.setPosition(charsetId);
        		charset = readCharset(input, charStringsIndex.getCount(), isCIDFont);
        	}
        }
        else
        {
        	// a CID font with no charset does not default to any predefined charset
        	if (isCIDFont)
        	{
        		// a CID font with no charset does not default to any predefined charset
        		charset = new EmptyCharset(charStringsIndex.getCount());
        	}
        	else
        	{
        		//FIXME PDFBOX-2571
        		charset = CFFISOAdobeCharset.getInstance();
        	}
        }
        font.setCharset(charset);

        // charstrings dict
        font.getCharStringBytes().add(charStringsIndex.getBytes(0)); // .notdef
        for (int i = 1; i < charStringsIndex.getCount(); i++)
        {
            byte[] bytes = charStringsIndex.getBytes(i);
            font.getCharStringBytes().add(bytes);
        }

        // format-specific dictionaries
        if (isCIDFont)
        {
        	parseCIDFontDicts(topDict, (CFFCIDFont) font, charStringsIndex);
        	// some malformed fonts have FontMatrix in their Font DICT, see PDFBOX-2495
        	if (topDict.getEntry("FontMatrix") == null)
        	{
        		List<Map<String, Object>> fontDicts = ((CFFCIDFont) font).getFontDicts();
        		if (fontDicts.size() > 0 && fontDicts.get(0).containsKey("FontMatrix"))
        		{
        			List<Number> matrix = (List<Number>)fontDicts.get(0).get("FontMatrix");
        			font.addValueToTopDict("FontMatrix", matrix);
        		}
        		else
        		{
        			// default
        			font.addValueToTopDict("FontMatrix", getArray(topDict, "FontMatrix",
        					Arrays.<Number>asList(0.001, (double) 0, (double) 0, 0.001,
        							(double) 0, (double) 0)));
        		}
        	}
        }
        else
        {
            parseType1Dicts(topDict, (CFFType1Font) font, charset);
        }

        return font;
    }

    /**
     * Parse dictionaries specific to a CIDFont.
     */
    private void parseCIDFontDicts(DictData topDict, CFFCIDFont font, IndexData charStringsIndex)
            throws IOException
    {
        // In a CIDKeyed Font, the Private dictionary isn't in the Top Dict but in the Font dict
        // which can be accessed by a lookup using FDArray and FDSelect
        DictData.Entry fdArrayEntry = topDict.getEntry("FDArray");
        if (fdArrayEntry == null)
        {
            throw new IOException("FDArray is missing for a CIDKeyed Font.");
        }

        // font dict index
        int fontDictOffset = fdArrayEntry.getNumber(0).intValue();
        input.setPosition(fontDictOffset);
        IndexData fdIndex = readIndexData(input);

        List<Map<String, Object>> privateDictionaries = new LinkedList<Map<String, Object>>();
        List<Map<String, Object>> fontDictionaries = new LinkedList<Map<String, Object>>();

        for (int i = 0; i < fdIndex.getCount(); ++i)
        {
            byte[] bytes = fdIndex.getBytes(i);
            CFFDataInput fontDictInput = new CFFDataInput(bytes);
            DictData fontDict = readDictData(fontDictInput);

            // font dict
            Map<String, Object> fontDictMap = new LinkedHashMap<String, Object>();
            fontDictMap.put("FontName", getString(fontDict, "FontName"));
            fontDictMap.put("FontType", getNumber(fontDict, "FontType", 0));
            fontDictMap.put("FontBBox", getDelta(fontDict, "FontBBox", null));
            fontDictMap.put("FontMatrix", getDelta(fontDict, "FontMatrix", null));
            // TODO OD-4 : Add here other keys
            fontDictionaries.add(fontDictMap);

            // read private dict
            DictData.Entry privateEntry = fontDict.getEntry("Private");
            if (privateEntry == null)
            {
                throw new IOException("Font DICT invalid without \"Private\" entry");
            }
            int privateOffset = privateEntry.getNumber(1).intValue();
            input.setPosition(privateOffset);
            int privateSize = privateEntry.getNumber(0).intValue();
            CFFDataInput privateDictData = new CFFDataInput(input.readBytes(privateSize));
            DictData privateDict = readDictData(privateDictData);

            // populate private dict
            Map<String, Object> privDict = readPrivateDict(privateDict);
            privateDictionaries.add(privDict);

            // local subrs
            int localSubrOffset = (Integer) getNumber(privateDict, "Subrs", 0);
            if (localSubrOffset == 0)
            {
                privDict.put("Subrs", new IndexData(0));
            }
            else
            {
                input.setPosition(privateOffset + localSubrOffset);
                IndexData idx = readIndexData(input);
                privDict.put("Subrs", idx);
            }
        }

        // font-dict (FD) select
        DictData.Entry fdSelectEntry = topDict.getEntry("FDSelect");
        int fdSelectPos = fdSelectEntry.getNumber(0).intValue();
        input.setPosition(fdSelectPos);
        FDSelect fdSelect = readFDSelect(input, charStringsIndex.getCount(), font);

        // TODO: almost certainly erroneous - CIDFonts do not have a top-level private dict
        // font.addValueToPrivateDict("defaultWidthX", 1000);
        // font.addValueToPrivateDict("nominalWidthX", 0);

        font.setFontDict(fontDictionaries);
        font.setPrivDict(privateDictionaries);
        font.setFdSelect(fdSelect);
    }
    
    private Map<String, Object> readPrivateDict(DictData privateDict)
    {
    	Map<String, Object> privDict = new LinkedHashMap<String, Object>();
    	privDict.put("BlueValues", getDelta(privateDict, "BlueValues", null));
    	privDict.put("OtherBlues", getDelta(privateDict, "OtherBlues", null));
    	privDict.put("FamilyBlues", getDelta(privateDict, "FamilyBlues", null));
    	privDict.put("FamilyOtherBlues", getDelta(privateDict, "FamilyOtherBlues", null));
    	privDict.put("BlueScale", getNumber(privateDict, "BlueScale", 0.039625));
    	privDict.put("BlueShift", getNumber(privateDict, "BlueShift", 7));
    	privDict.put("BlueFuzz", getNumber(privateDict, "BlueFuzz", 1));
    	privDict.put("StdHW", getNumber(privateDict, "StdHW", null));
    	privDict.put("StdVW", getNumber(privateDict, "StdVW", null));
    	privDict.put("StemSnapH", getDelta(privateDict, "StemSnapH", null));
    	privDict.put("StemSnapV", getDelta(privateDict, "StemSnapV", null));
    	privDict.put("ForceBold", getBoolean(privateDict, "ForceBold", false));
    	privDict.put("LanguageGroup", getNumber(privateDict, "LanguageGroup", 0));
    	privDict.put("ExpansionFactor", getNumber(privateDict, "ExpansionFactor", 0.06));
    	privDict.put("initialRandomSeed", getNumber(privateDict, "initialRandomSeed", 0));
    	privDict.put("defaultWidthX", getNumber(privateDict, "defaultWidthX", 0));
    	privDict.put("nominalWidthX", getNumber(privateDict, "nominalWidthX", 0));
    	return privDict;
    }

    /**
     * Parse dictionaries specific to a Type 1-equivalent font.
     */
    private void parseType1Dicts(DictData topDict, CFFType1Font font, CFFCharset charset)
            throws IOException
    {
        // encoding
        DictData.Entry encodingEntry = topDict.getEntry("Encoding");
        CFFEncoding encoding;
        int encodingId = encodingEntry != null ? encodingEntry.getNumber(0).intValue() : 0;
        if (encodingId == 0)
        {
            encoding = CFFStandardEncoding.getInstance();
        }
        else if (encodingId == 1)
        {
            encoding = CFFExpertEncoding.getInstance();
        }
        else
        {
            input.setPosition(encodingId);
            encoding = readEncoding(input, charset);
        }
        font.setEncoding(encoding);

        // read private dict
        DictData.Entry privateEntry = topDict.getEntry("Private");
        int privateOffset = privateEntry.getNumber(1).intValue();
        input.setPosition(privateOffset);
        int privateSize = privateEntry.getNumber(0).intValue();
        CFFDataInput privateDictData = new CFFDataInput(input.readBytes(privateSize));
        DictData privateDict = readDictData(privateDictData);

        // populate private dict
        Map<String, Object> privDict = readPrivateDict(privateDict);
        for (Map.Entry<String, Object> entry : privDict.entrySet())
        {
        	font.addToPrivateDict(entry.getKey(), entry.getValue());
        }

        // local subrs
        int localSubrOffset = (Integer) getNumber(privateDict, "Subrs", 0);
        if (localSubrOffset == 0)
        {
            font.addToPrivateDict("Subrs", new IndexData(0));
        }
        else
        {
            input.setPosition(privateOffset + localSubrOffset);
            font.addToPrivateDict("Subrs", readIndexData(input));
        }
    }

    private String readString(int index) throws IOException
    {
        if (index >= 0 && index <= 390)
        {
            return CFFStandardString.getName(index);
        }
        if (index - 391 < stringIndex.getCount())
        {
            DataInput dataInput = new DataInput(stringIndex.getBytes(index - 391));
            return dataInput.getString();
        }
        // technically this maps to .notdef, but we need a unique sid name
        return "SID" + index;
    }

    private String getString(DictData dict, String name) throws IOException
    {
        DictData.Entry entry = dict.getEntry(name);
        return entry != null ? readString(entry.getNumber(0).intValue()) : null;
    }

    private static Boolean getBoolean(DictData dict, String name, boolean defaultValue)
    {
        DictData.Entry entry = dict.getEntry(name);
        return entry != null ? entry.getBoolean(0) : defaultValue;
    }

    private static Number getNumber(DictData dict, String name, Number defaultValue)
    {
        DictData.Entry entry = dict.getEntry(name);
        return entry != null ? entry.getNumber(0) : defaultValue;
    }

    // TODO Where is the difference to getDelta??
    private static List<Number> getArray(DictData dict, String name, List<Number> defaultValue)
    {
        DictData.Entry entry = dict.getEntry(name);
        return entry != null ? entry.getArray() : defaultValue;
    }

    // TODO Where is the difference to getArray??
    private static List<Number> getDelta(DictData dict, String name, List<Number> defaultValue)
    {
        DictData.Entry entry = dict.getEntry(name);
        return entry != null ? entry.getArray() : defaultValue;
    }

    private CFFEncoding readEncoding(CFFDataInput dataInput, CFFCharset charset) throws IOException
    {
        int format = dataInput.readCard8();
        int baseFormat = format & 0x7f;

        if (baseFormat == 0)
        {
            return readFormat0Encoding(dataInput, charset, format);
        }
        else if (baseFormat == 1)
        {
            return readFormat1Encoding(dataInput, charset, format);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    private Format0Encoding readFormat0Encoding(CFFDataInput dataInput, CFFCharset charset,
                                                int format) throws IOException
    {
        Format0Encoding encoding = new Format0Encoding();
        encoding.format = format;
        encoding.nCodes = dataInput.readCard8();
        encoding.code = new int[encoding.nCodes];
        encoding.add(0, 0, ".notdef");
        for (int gid = 1; gid <= encoding.nCodes; gid++)
        {
            int code = dataInput.readCard8();
            encoding.code[gid - 1] = code;
            int sid = charset.getSIDForGID(gid);
            encoding.add(code, sid, readString(sid));
        }
        if ((format & 0x80) != 0)
        {
            readSupplement(dataInput, encoding);
        }
        return encoding;
    }

    private Format1Encoding readFormat1Encoding(CFFDataInput dataInput, CFFCharset charset,
                                                int format) throws IOException
    {
        Format1Encoding encoding = new Format1Encoding();
        encoding.format = format;
        encoding.nRanges = dataInput.readCard8();
        encoding.range = new Format1Encoding.Range1[encoding.nRanges];
        encoding.add(0, 0, ".notdef");
        int gid = 1;
        for (int i = 0; i < encoding.range.length; i++)
        {
            Format1Encoding.Range1 range = new Format1Encoding.Range1();
            range.first = dataInput.readCard8();
            range.nLeft = dataInput.readCard8();
            encoding.range[i] = range;
            for (int j = 0; j < 1 + range.nLeft; j++)
            {
                int sid = charset.getSIDForGID(gid);
                int code = range.first + j;
                encoding.add(code, sid, readString(sid));
                gid++;
            }
        }
        if ((format & 0x80) != 0)
        {
            readSupplement(dataInput, encoding);
        }
        return encoding;
    }

    private void readSupplement(CFFDataInput dataInput, CFFBuiltInEncoding encoding) throws IOException
    {
        encoding.nSups = dataInput.readCard8();
        encoding.supplement = new CFFBuiltInEncoding.Supplement[encoding.nSups];
        for (int i = 0; i < encoding.supplement.length; i++)
        {
            CFFBuiltInEncoding.Supplement supplement = new CFFBuiltInEncoding.Supplement();
            supplement.code = dataInput.readCard8();
            supplement.sid = dataInput.readSID();
            supplement.name = readString(supplement.sid);
            encoding.supplement[i] = supplement;
            encoding.add(supplement.code, supplement.sid, readString(supplement.sid));
        }
    }

    /**
     * Read the FDSelect Data according to the format.
     * @param dataInput
     * @param nGlyphs
     * @param ros
     * @return the FDSelect data
     * @throws IOException
     */
    private static FDSelect readFDSelect(CFFDataInput dataInput, int nGlyphs, CFFCIDFont ros) throws IOException
    {
        int format = dataInput.readCard8();
        if (format == 0)
        {
            return readFormat0FDSelect(dataInput, format, nGlyphs, ros);
        }
        else if (format == 3)
        {
            return readFormat3FDSelect(dataInput, format, nGlyphs, ros);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Read the Format 0 of the FDSelect data structure.
     * @param dataInput
     * @param format
     * @param nGlyphs
     * @param ros
     * @return the Format 0 of the FDSelect data
     * @throws IOException
     */
    private static Format0FDSelect readFormat0FDSelect(CFFDataInput dataInput, int format, int nGlyphs, CFFCIDFont ros)
            throws IOException
    {
        Format0FDSelect fdselect = new Format0FDSelect(ros);
        fdselect.format = format;
        fdselect.fds = new int[nGlyphs];
        for (int i = 0; i < fdselect.fds.length; i++)
        {
            fdselect.fds[i] = dataInput.readCard8();

        }
        return fdselect;
    }

    /**
     * Read the Format 3 of the FDSelect data structure.
     * 
     * @param dataInput
     * @param format
     * @param nGlyphs
     * @param ros
     * @return the Format 3 of the FDSelect data
     * @throws IOException
     */
    private static Format3FDSelect readFormat3FDSelect(CFFDataInput dataInput, int format, int nGlyphs, CFFCIDFont ros)
            throws IOException
    {
        Format3FDSelect fdselect = new Format3FDSelect(ros);
        fdselect.format = format;
        fdselect.nbRanges = dataInput.readCard16();

        fdselect.range3 = new Range3[fdselect.nbRanges];
        for (int i = 0; i < fdselect.nbRanges; i++)
        {
            Range3 r3 = new Range3();
            r3.first = dataInput.readCard16();
            r3.fd = dataInput.readCard8();
            fdselect.range3[i] = r3;

        }

        fdselect.sentinel = dataInput.readCard16();
        return fdselect;
    }

    /**
     *  Format 3 FDSelect data.
     */
    private static final class Format3FDSelect extends FDSelect
    {
        private int format;
        private int nbRanges;
        private Range3[] range3;
        private int sentinel;

        private Format3FDSelect(CFFCIDFont owner)
        {
            super(owner);
        }

        @Override
        public int getFDIndex(int gid)
        {
            for (int i = 0; i < nbRanges; ++i)
            {
                if (range3[i].first <= gid)
                {
                    if (i + 1 < nbRanges)
                    {
                        if (range3[i + 1].first > gid)
                        {
                            return range3[i].fd;
                        }
                        // go to next range
                    }
                    else
                    {
                        // last range reach, the sentinel must be greater than gid
                        if (sentinel > gid)
                        {
                            return range3[i].fd;
                        }
                        return -1;
                    }
                }
            }
            return 0;
        }

        @Override
        public String toString()
        {
            return getClass().getName() + "[format=" + format + " nbRanges=" + nbRanges + ", range3="
                    + Arrays.toString(range3) + " sentinel=" + sentinel + "]";
        }
    }

    /**
     * Structure of a Range3 element.
     */
    private static final class Range3
    {
        private int first;
        private int fd;

        @Override
        public String toString()
        {
            return getClass().getName() + "[first=" + first + ", fd=" + fd + "]";
        }
    }

    /**
     *  Format 0 FDSelect.
     */
    private static class Format0FDSelect extends FDSelect
    {
        private int format;
        private int[] fds;

        private Format0FDSelect(CFFCIDFont owner)
        {
            super(owner);
        }

        @Override
        public int getFDIndex(int gid)
        {
            if (gid < fds.length)
            {
                return fds[gid];
            }
            return 0;
        }

        @Override
        public String toString()
        {
            return getClass().getName() + "[fds=" + Arrays.toString(fds) + "]";
        }
    }

    private CFFCharset readCharset(CFFDataInput dataInput, int nGlyphs, boolean isCIDFont)
            throws IOException
    {
        int format = dataInput.readCard8();
        if (format == 0)
        {
            return readFormat0Charset(dataInput, format, nGlyphs, isCIDFont);
        }
        else if (format == 1)
        {
            return readFormat1Charset(dataInput, format, nGlyphs, isCIDFont);
        }
        else if (format == 2)
        {
            return readFormat2Charset(dataInput, format, nGlyphs, isCIDFont);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    private Format0Charset readFormat0Charset(CFFDataInput dataInput, int format, int nGlyphs,
                                              boolean isCIDFont) throws IOException
    {
        Format0Charset charset = new Format0Charset(isCIDFont);
        charset.format = format;
        charset.glyph = new int[nGlyphs];
        charset.glyph[0] = 0;
        if (isCIDFont)
        {
            charset.addCID(0, 0);
        }
        else
        {
            charset.addSID(0, 0, ".notdef");
        }

        for (int gid = 1; gid < charset.glyph.length; gid++)
        {
            int sid = dataInput.readSID();
            charset.glyph[gid] = sid;
            if (isCIDFont)
            {
                charset.addCID(gid, sid);
            }
            else
            {
                charset.addSID(gid, sid, readString(sid));
            }
        }
        return charset;
    }

    private Format1Charset readFormat1Charset(CFFDataInput dataInput, int format, int nGlyphs,
                                              boolean isCIDFont) throws IOException
    {
        Format1Charset charset = new Format1Charset(isCIDFont);
        charset.format = format;
        List<Format1Charset.Range1> ranges = new ArrayList<Format1Charset.Range1>();
        if (isCIDFont)
        {
            charset.addCID(0, 0);
        }
        else
        {
            charset.addSID(0, 0, ".notdef");
        }

        for (int gid = 1; gid < nGlyphs; gid++)
        {
            Format1Charset.Range1 range = new Format1Charset.Range1();
            range.first = dataInput.readSID();
            range.nLeft = dataInput.readCard8();
            ranges.add(range);
            for (int j = 0; j < 1 + range.nLeft; j++)
            {
                int sid = range.first + j;
                if (isCIDFont)
                {
                    charset.addCID(gid + j, sid);
                }
                else
                {
                    charset.addSID(gid + j, sid, readString(sid));
                }
            }
            gid += range.nLeft;
        }
        charset.range = ranges.toArray(new Format1Charset.Range1[0]);
        return charset;
    }

    private Format2Charset readFormat2Charset(CFFDataInput dataInput, int format, int nGlyphs,
                                              boolean isCIDFont) throws IOException
    {
        Format2Charset charset = new Format2Charset(isCIDFont);
        charset.format = format;
        charset.range = new Format2Charset.Range2[0];
        if (isCIDFont)
        {
            charset.addCID(0, 0);
        }
        else
        {
            charset.addSID(0, 0, ".notdef");
        }

        for (int gid = 1; gid < nGlyphs; gid++)
        {
            Format2Charset.Range2[] newRange = new Format2Charset.Range2[charset.range.length + 1];
            System.arraycopy(charset.range, 0, newRange, 0, charset.range.length);
            charset.range = newRange;
            Format2Charset.Range2 range = new Format2Charset.Range2();
            range.first = dataInput.readSID();
            range.nLeft = dataInput.readCard16();
            charset.range[charset.range.length - 1] = range;
            for (int j = 0; j < 1 + range.nLeft; j++)
            {
                int sid = range.first + j;
                if (isCIDFont)
                {
                    charset.addCID(gid + j, sid);
                }
                else
                {
                    charset.addSID(gid + j, sid, readString(sid));
                }
            }
            gid += range.nLeft;
        }
        return charset;
    }

    /**
     * Inner class holding the header of a CFF font. 
     */
    private static class Header
    {
        private int major;
        private int minor;
        private int hdrSize;
        private int offSize;

        @Override
        public String toString()
        {
            return getClass().getName() + "[major=" + major + ", minor=" + minor + ", hdrSize=" + hdrSize
                    + ", offSize=" + offSize + "]";
        }
    }

    /**
     * Inner class holding the DictData of a CFF font. 
     */
    private static class DictData
    {

        private List<Entry> entries = null;

        public Entry getEntry(CFFOperator.Key key)
        {
            return getEntry(CFFOperator.getOperator(key));
        }

        public Entry getEntry(String name)
        {
            return getEntry(CFFOperator.getOperator(name));
        }

        private Entry getEntry(CFFOperator operator)
        {
            for (Entry entry : entries)
            {
                // Check for null entry before comparing the Font
                if (entry != null && entry.operator != null && entry.operator.equals(operator))
                {
                    return entry;
                }
            }
            return null;
        }

        /**
         * {@inheritDoc} 
         */
        @Override
        public String toString()
        {
            return getClass().getName() + "[entries=" + entries + "]";
        }

        /**
         * Inner class holding an operand of a CFF font. 
         */
        private static class Entry
        {
            private List<Number> operands = new ArrayList<Number>();
            private CFFOperator operator = null;

            public Number getNumber(int index)
            {
                return operands.get(index);
            }

            public Boolean getBoolean(int index)
            {
                Number operand = operands.get(index);
                if (operand instanceof Integer)
                {
                    switch (operand.intValue())
                    {
                    case 0:
                        return Boolean.FALSE;
                    case 1:
                        return Boolean.TRUE;
                    default:
                        break;
                    }
                }
                throw new IllegalArgumentException();
            }

            // TODO unused??
            public Integer getSID(int index)
            {
                Number operand = operands.get(index);
                if (operand instanceof Integer)
                {
                    return (Integer) operand;
                }
                throw new IllegalArgumentException();
            }

            // TODO Where is the difference to getDelta??
            public List<Number> getArray()
            {
                return operands;
            }

            // TODO Where is the difference to getArray??
            public List<Number> getDelta()
            {
                return operands;
            }

            @Override
            public String toString()
            {
                return getClass().getName() + "[operands=" + operands + ", operator=" + operator + "]";
            }
        }
    }

    /**
     * Inner class representing a font's built-in CFF encoding.
     */
    abstract static class CFFBuiltInEncoding extends CFFEncoding
    {
        private int nSups;
        private Supplement[] supplement;

        /**
         * Inner class representing a supplement for an encoding. 
         */
        static class Supplement
        {
            private int code;
            private int sid;
            private String name;

            public int getCode()
            {
                return code;
            }

            public int getSID()
            {
                return sid;
            }

            public String getName()
            {
                return name;
            }

            @Override
            public String toString()
            {
                return getClass().getName() + "[code=" + code + ", sid=" + sid + "]";
            }
        }
    }

    /**
     * Inner class representing a Format0 encoding. 
     */
    private static class Format0Encoding extends CFFBuiltInEncoding
    {
        private int format;
        private int nCodes;
        private int[] code;

        @Override
        public String toString()
        {
            return getClass().getName() + "[format=" + format + ", nCodes=" + nCodes + ", code="
                    + Arrays.toString(code) + ", supplement=" + Arrays.toString(super.supplement) + "]";
        }
    }

    /**
     * Inner class representing a Format1 encoding. 
     */
    private static class Format1Encoding extends CFFBuiltInEncoding
    {
        private int format;
        private int nRanges;
        private Range1[] range;

        @Override
        public String toString()
        {
            return getClass().getName() + "[format=" + format + ", nRanges=" + nRanges + ", range="
                    + Arrays.toString(range) + ", supplement=" + Arrays.toString(super.supplement) + "]";
        }

        /**
         * Inner class representing a range of an encoding. 
         */
        private static class Range1
        {
            private int first;
            private int nLeft;

            @Override
            public String toString()
            {
                return getClass().getName() + "[first=" + first + ", nLeft=" + nLeft + "]";
            }
        }
    }

    /**
     * Inner class representing an embedded CFF charset.
     */
    abstract static class EmbeddedCharset extends CFFCharset
    {
        protected EmbeddedCharset(boolean isCIDFont)
        {
            super(isCIDFont);
        }
    }

    /**
     * An empty charset in a malformed CID font.
     */
    private static class EmptyCharset extends EmbeddedCharset
    {
    	protected EmptyCharset(int numCharStrings)
    	{
    		super(true);
    		addCID(0 ,0); // .notdef

    		// Adobe Reader treats CID as GID, PDFBOX-2571 p11.
    		for (int i = 1; i <= numCharStrings; i++)
    		{
    			addCID(i, i);
    		}
    	}

    	@Override
    	public String toString()
    	{
    		return getClass().getName();
    	}
    }

    /**
     * Inner class representing a Format0 charset. 
     */
    private static class Format0Charset extends EmbeddedCharset
    {
        private int format;
        private int[] glyph;

        protected Format0Charset(boolean isCIDFont)
        {
            super(isCIDFont);
        }

        @Override
        public String toString()
        {
            return getClass().getName() + "[format=" + format + ", glyph=" + Arrays.toString(glyph) + "]";
        }
    }

    /**
     * Inner class representing a Format1 charset. 
     */
    private static class Format1Charset extends EmbeddedCharset
    {
        private int format;
        private Range1[] range;

        protected Format1Charset(boolean isCIDFont)
        {
            super(isCIDFont);
        }

        @Override
        public String toString()
        {
            return getClass().getName() + "[format=" + format + ", range=" + Arrays.toString(range) + "]";
        }

        /**
         * Inner class representing a range of a charset. 
         */
        private static class Range1
        {
            private int first;
            private int nLeft;

            @Override
            public String toString()
            {
                return getClass().getName() + "[first=" + first + ", nLeft=" + nLeft + "]";
            }
        }
    }

    /**
     * Inner class representing a Format2 charset. 
     */
    private static class Format2Charset extends EmbeddedCharset
    {
        private int format;
        private Range2[] range;

        protected Format2Charset(boolean isCIDFont)
        {
            super(isCIDFont);
        }

        @Override
        public String toString()
        {
            return getClass().getName() + "[format=" + format + ", range=" + Arrays.toString(range) + "]";
        }

        /**
         * Inner class representing a range of a charset. 
         */
        private static class Range2
        {
            private int first;
            private int nLeft;

            @Override
            public String toString()
            {
                return getClass().getName() + "[first=" + first + ", nLeft=" + nLeft + "]";
            }
        }
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "[" + debugFontName + "]";
    }
}
