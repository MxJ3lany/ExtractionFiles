/*

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package com.tom_roush.fontbox.ttf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.util.Log;

/**
 * Glyph description for composite glyphs. Composite glyphs are made up of one
 * or more simple glyphs, usually with some sort of transformation applied to
 * each.
 *
 * This class is based on code from Apache Batik a subproject of Apache
 * XMLGraphics. see http://xmlgraphics.apache.org/batik/ for further details.
 */
public class GlyfCompositeDescript extends GlyfDescript
{
    private final List<GlyfCompositeComp> components = new ArrayList<GlyfCompositeComp>();
    private GlyphTable glyphTable = null;
    private boolean beingResolved = false;
    private boolean resolved = false;

    /**
     * Constructor.
     * 
     * @param bais the stream to be read
     * @param glyphTable the Glyphtable containing all glyphs
     * @throws IOException is thrown if something went wrong
     */
    public GlyfCompositeDescript(TTFDataStream bais, GlyphTable glyphTable) throws IOException
    {
        super((short) -1, bais);

        this.glyphTable = glyphTable;

        // Get all of the composite components
        GlyfCompositeComp comp;
        do
        {
            comp = new GlyfCompositeComp(bais);
            components.add(comp);
        } 
        while ((comp.getFlags() & GlyfCompositeComp.MORE_COMPONENTS) != 0);

        // Are there hinting instructions to read?
        if ((comp.getFlags() & GlyfCompositeComp.WE_HAVE_INSTRUCTIONS) != 0)
        {
            readInstructions(bais, (bais.readUnsignedShort()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resolve()
    {
        if (resolved)
        {
            return;
        }
        if (beingResolved)
        {
        	Log.e("PdfBox-Android", "Circular reference in GlyfCompositeDesc");
            return;
        }
        beingResolved = true;

        int firstIndex = 0;
        int firstContour = 0;

        Iterator<GlyfCompositeComp> i = components.iterator();
        while (i.hasNext())
        {
            GlyfCompositeComp comp = i.next();
            comp.setFirstIndex(firstIndex);
            comp.setFirstContour(firstContour);

            GlyphDescription desc;
            desc = getGlypDescription(comp.getGlyphIndex());
            if (desc != null)
            {
                desc.resolve();
                firstIndex += desc.getPointCount();
                firstContour += desc.getContourCount();
            }
        }
        resolved = true;
        beingResolved = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getEndPtOfContours(int i)
    {
        GlyfCompositeComp c = getCompositeCompEndPt(i);
        if (c != null)
        {
            GlyphDescription gd = getGlypDescription(c.getGlyphIndex());
            return gd.getEndPtOfContours(i - c.getFirstContour()) + c.getFirstIndex();
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte getFlags(int i)
    {
        GlyfCompositeComp c = getCompositeComp(i);
        if (c != null)
        {
            GlyphDescription gd = getGlypDescription(c.getGlyphIndex());
            return gd.getFlags(i - c.getFirstIndex());
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short getXCoordinate(int i)
    {
        GlyfCompositeComp c = getCompositeComp(i);
        if (c != null)
        {
            GlyphDescription gd = getGlypDescription(c.getGlyphIndex());
            int n = i - c.getFirstIndex();
            int x = gd.getXCoordinate(n);
            int y = gd.getYCoordinate(n);
            short x1 = (short) c.scaleX(x, y);
            x1 += c.getXTranslate();
            return x1;
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short getYCoordinate(int i)
    {
        GlyfCompositeComp c = getCompositeComp(i);
        if (c != null)
        {
            GlyphDescription gd = getGlypDescription(c.getGlyphIndex());
            int n = i - c.getFirstIndex();
            int x = gd.getXCoordinate(n);
            int y = gd.getYCoordinate(n);
            short y1 = (short) c.scaleY(x, y);
            y1 += c.getYTranslate();
            return y1;
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isComposite()
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPointCount()
    {
        if (!resolved)
        {
        	Log.e("PdfBox-Android", "getPointCount called on unresolved GlyfCompositeDescript");
        }
        GlyfCompositeComp c = components.get(components.size() - 1);
        GlyphDescription gd = getGlypDescription(c.getGlyphIndex());
        if (gd == null)
        {
        	Log.e("PdfBox-Android", "getGlypDescription(" + c.getGlyphIndex() + ") is null, returning 0");
            return 0;
        }
        return c.getFirstIndex() + gd.getPointCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getContourCount()
    {
        if (!resolved)
        {
        	Log.e("PdfBox-Android", "getContourCount called on unresolved GlyfCompositeDescript");
        }
        GlyfCompositeComp c = components.get(components.size() - 1);
        return c.getFirstContour() + getGlypDescription(c.getGlyphIndex()).getContourCount();
    }

    /**
     * Get number of components.
     * 
     * @return the number of components
     */
    public int getComponentCount()
    {
        return components.size();
    }

    private GlyfCompositeComp getCompositeComp(int i)
    {
        for (GlyfCompositeComp c : components)
        {
            GlyphDescription gd = getGlypDescription(c.getGlyphIndex());
            if (c.getFirstIndex() <= i && i < (c.getFirstIndex() + gd.getPointCount()))
            {
                return c;
            }
        }
        return null;
    }

    private GlyfCompositeComp getCompositeCompEndPt(int i)
    {
        for (GlyfCompositeComp c : components)
        {
            GlyphDescription gd = getGlypDescription(c.getGlyphIndex());
            if (c.getFirstContour() <= i && i < (c.getFirstContour() + gd.getContourCount()))
            {
                return c;
            }
        }
        return null;
    }

    private GlyphDescription getGlypDescription(int index)
    {
        try
        {
            GlyphData glyph = glyphTable.getGlyph(index);
            if (glyph != null)
            {
                return glyph.getDescription();
            }
            return null;
        }
        catch (IOException e)
        {
        	Log.e("PdfBox-Android", e.getMessage());
            return null;
        }
    }
}
