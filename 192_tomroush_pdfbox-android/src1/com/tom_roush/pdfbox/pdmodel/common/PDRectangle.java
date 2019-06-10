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
package com.tom_roush.pdfbox.pdmodel.common;

import android.graphics.Path;
import android.graphics.PointF;

import com.tom_roush.fontbox.util.BoundingBox;
import com.tom_roush.pdfbox.cos.COSArray;
import com.tom_roush.pdfbox.cos.COSBase;
import com.tom_roush.pdfbox.cos.COSFloat;
import com.tom_roush.pdfbox.cos.COSNumber;
import com.tom_roush.pdfbox.util.Matrix;

/**
 * A rectangle in a PDF document.
 *
 * @author Ben Litchfield
 *
 */
public class PDRectangle implements COSObjectable
{
    private static final float POINTS_PER_INCH = 72;
    private static final float MM_PER_INCH = 1 / (10 * 2.54f) * POINTS_PER_INCH;

    /** A rectangle the size of U.S. Letter, 8.5" x 11". */
    public static final PDRectangle LETTER = new PDRectangle(8.5f * POINTS_PER_INCH,
        11f *POINTS_PER_INCH);
    /**  A rectangle the size of U.S. Legal, 8.5" x 14". */
    public static final PDRectangle LEGAL = new PDRectangle(8.5f * POINTS_PER_INCH,
        14f * POINTS_PER_INCH);
    /**  A rectangle the size of A0 Paper. */
    public static final PDRectangle A0 = new PDRectangle(841 * MM_PER_INCH, 1189 * MM_PER_INCH);

    /** A rectangle the size of A1 Paper. */
    public static final PDRectangle A1 = new PDRectangle(594 * MM_PER_INCH, 841 * MM_PER_INCH);

    /**  A rectangle the size of A2 Paper. */
    public static final PDRectangle A2 = new PDRectangle(420 * MM_PER_INCH, 594 * MM_PER_INCH);

    /** A rectangle the size of A3 Paper.  */
    public static final PDRectangle A3 = new PDRectangle(297 * MM_PER_INCH, 420 * MM_PER_INCH);

    /**  A rectangle the size of A4 Paper. */
    public static final PDRectangle A4 = new PDRectangle(210 * MM_PER_INCH, 297 * MM_PER_INCH);

    /** A rectangle the size of A5 Paper. */
    public static final PDRectangle A5 = new PDRectangle(148 * MM_PER_INCH, 210 * MM_PER_INCH);

    /**  A rectangle the size of A6 Paper. */
    public static final PDRectangle A6 = new PDRectangle(105 * MM_PER_INCH, 148 * MM_PER_INCH);

    private final COSArray rectArray;

    /**
     * Constructor.
     *
     * Initializes to 0,0,0,0
     */
    public PDRectangle()
    {
        this(0.0f, 0.0f, 0.0f, 0.0f);
    }

    /**
     * Constructor.
     *
     * @param width The width of the rectangle.
     * @param height The height of the rectangle.
     */
    public PDRectangle( float width, float height )
    {
        this(0.0f, 0.0f, width, height);
    }

    /**
     * Constructor.
     *
     * @param x the x coordinate of the rectangle
     * @param y the y coordinate of the rectangle
     * @param width The width of the rectangle.
     * @param height The height of the rectangle.
     */
    public PDRectangle( float x, float y, float width, float height )
    {
        rectArray = new COSArray();
        rectArray.add( new COSFloat( x ) );
        rectArray.add( new COSFloat( y ) );
        rectArray.add( new COSFloat( x + width ) );
        rectArray.add( new COSFloat( y + height ) );
    }

    /**
     * Constructor.
     *
     * @param box the bounding box to be used for the rectangle
     */
    public PDRectangle( BoundingBox box )
    {
        rectArray = new COSArray();
        rectArray.add( new COSFloat( box.getLowerLeftX() ) );
        rectArray.add( new COSFloat( box.getLowerLeftY() ) );
        rectArray.add( new COSFloat( box.getUpperRightX() ) );
        rectArray.add( new COSFloat( box.getUpperRightY() ) );
    }

    /**
     * Constructor.
     *
     * @param array An array of numbers as specified in the PDF Reference for a rectangle type.
     */
    public PDRectangle( COSArray array )
    {
        float[] values = array.toFloatArray();
        rectArray = new COSArray();
        // we have to start with the lower left corner
        rectArray.add( new COSFloat( Math.min(values[0],values[2] )) );
        rectArray.add( new COSFloat( Math.min(values[1],values[3] )) );
        rectArray.add( new COSFloat( Math.max(values[0],values[2] )) );
        rectArray.add( new COSFloat( Math.max(values[1],values[3] )) );
    }

    /**
     * Method to determine if the x/y point is inside this rectangle.
     * @param x The x-coordinate to test.
     * @param y The y-coordinate to test.
     * @return True if the point is inside this rectangle.
     */
    public boolean contains( float x, float y )
    {
        float llx = getLowerLeftX();
        float urx = getUpperRightX();
        float lly = getLowerLeftY();
        float ury = getUpperRightY();
        return x >= llx && x <= urx &&
            y >= lly && y <= ury;
    }

    /**
     * This will create a translated rectangle based off of this rectangle, such
     * that the new rectangle retains the same dimensions(height/width), but the
     * lower left x,y values are zero. <br />
     * 100, 100, 400, 400 (llx, lly, urx, ury ) <br />
     * will be translated to 0,0,300,300
     *
     * @return A new rectangle that has been translated back to the origin.
     */
    public PDRectangle createRetranslatedRectangle()
    {
        PDRectangle retval = new PDRectangle();
        retval.setUpperRightX( getWidth() );
        retval.setUpperRightY( getHeight() );
        return retval;
    }

    /**
     * This will get the underlying array for this rectangle.
     *
     * @return The cos array.
     */
    public COSArray getCOSArray()
    {
        return rectArray;
    }

    /**
     * This will get the lower left x coordinate.
     *
     * @return The lower left x.
     */
    public float getLowerLeftX()
    {
        return ((COSNumber)rectArray.get(0)).floatValue();
    }

    /**
     * This will set the lower left x coordinate.
     *
     * @param value The lower left x.
     */
    public void setLowerLeftX(float value)
    {
        rectArray.set(0, new COSFloat( value ) );
    }

    /**
     * This will get the lower left y coordinate.
     *
     * @return The lower left y.
     */
    public float getLowerLeftY()
    {
        return ((COSNumber)rectArray.get(1)).floatValue();
    }

    /**
     * This will set the lower left y coordinate.
     *
     * @param value The lower left y.
     */
    public void setLowerLeftY(float value)
    {
        rectArray.set(1, new COSFloat( value ) );
    }

    /**
     * This will get the upper right x coordinate.
     *
     * @return The upper right x .
     */
    public float getUpperRightX()
    {
        return ((COSNumber)rectArray.get(2)).floatValue();
    }

    /**
     * This will set the upper right x coordinate.
     *
     * @param value The upper right x .
     */
    public void setUpperRightX(float value)
    {
        rectArray.set(2, new COSFloat( value ) );
    }

    /**
     * This will get the upper right y coordinate.
     *
     * @return The upper right y.
     */
    public float getUpperRightY()
    {
        return ((COSNumber)rectArray.get(3)).floatValue();
    }

    /**
     * This will set the upper right y coordinate.
     *
     * @param value The upper right y.
     */
    public void setUpperRightY(float value)
    {
        rectArray.set(3, new COSFloat( value ) );
    }

    /**
     * This will get the width of this rectangle as calculated by
     * upperRightX - lowerLeftX.
     *
     * @return The width of this rectangle.
     */
    public float getWidth()
    {
        return getUpperRightX() - getLowerLeftX();
    }

    /**
     * This will get the height of this rectangle as calculated by
     * upperRightY - lowerLeftY.
     *
     * @return The height of this rectangle.
     */
    public float getHeight()
    {
        return getUpperRightY() - getLowerLeftY();
    }

    /**
     * This will move the rectangle the given relative amount.
     *
     * @param horizontalAmount positive values will move rectangle to the right, negative's to the left.
     * @param verticalAmount positive values will move the rectangle up, negative's down.
     */
    public void move(float horizontalAmount, float verticalAmount)
    {
        setUpperRightX(getUpperRightX() + horizontalAmount);
        setLowerLeftX(getLowerLeftX() + horizontalAmount);
        setUpperRightY(getUpperRightY() + verticalAmount);
        setLowerLeftY(getLowerLeftY() + verticalAmount);
    }

    /**
     * Returns a copy of this rectangle which has been transformed using the given matrix.
     */
    public Path transform(Matrix matrix)
    {
        float x1 = getLowerLeftX();
        float y1 = getLowerLeftY();
        float x2 = getUpperRightX();
        float y2 = getUpperRightY();

        PointF p0 = matrix.transformPoint(x1, y1);
        PointF p1 = matrix.transformPoint(x2, y1);
        PointF p2 = matrix.transformPoint(x2, y2);
        PointF p3 = matrix.transformPoint(x1, y2);

        Path path = new Path();
        path.moveTo(p0.x, p0.y);
        path.lineTo(p1.x, p1.y);
        path.lineTo(p2.x, p2.y);
        path.lineTo(p3.x, p3.y);
        path.close();
        return path;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    @Override
    public COSBase getCOSObject()
    {
        return rectArray;
    }

    /**
     * Returns a general path equivalent to this rectangle. This method avoids the problems
     * caused by Rectangle2D not working well with -ve rectangles.
     */
    public Path toGeneralPath()
    {
        float x1 = getLowerLeftX();
        float y1 = getLowerLeftY();
        float x2 = getUpperRightX();
        float y2 = getUpperRightY();
        Path path = new Path();
        path.moveTo(x1, y1);
        path.lineTo(x2, y1);
        path.lineTo(x2, y2);
        path.lineTo(x1, y2);
        path.close();
        return path;
    }

    /**
     * This will return a string representation of this rectangle.
     *
     * @return This object as a string.
     */
    @Override
    public String toString()
    {
        return "[" + getLowerLeftX() + "," + getLowerLeftY() + "," +
            getUpperRightX() + "," + getUpperRightY() +"]";
    }
}
