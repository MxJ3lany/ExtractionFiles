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
package com.tom_roush.pdfbox.contentstream;

import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.tom_roush.pdfbox.contentstream.operator.MissingOperandException;
import com.tom_roush.pdfbox.contentstream.operator.Operator;
import com.tom_roush.pdfbox.contentstream.operator.OperatorProcessor;
import com.tom_roush.pdfbox.contentstream.operator.state.EmptyGraphicsStackException;
import com.tom_roush.pdfbox.cos.COSArray;
import com.tom_roush.pdfbox.cos.COSBase;
import com.tom_roush.pdfbox.cos.COSNumber;
import com.tom_roush.pdfbox.cos.COSObject;
import com.tom_roush.pdfbox.cos.COSString;
import com.tom_roush.pdfbox.filter.MissingImageReaderException;
import com.tom_roush.pdfbox.pdfparser.PDFStreamParser;
import com.tom_roush.pdfbox.pdmodel.MissingResourceException;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDResources;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.pdmodel.font.PDFontFactory;
import com.tom_roush.pdfbox.pdmodel.font.PDType3CharProc;
import com.tom_roush.pdfbox.pdmodel.font.PDType3Font;
import com.tom_roush.pdfbox.pdmodel.graphics.PDLineDashPattern;
import com.tom_roush.pdfbox.pdmodel.graphics.color.PDColor;
import com.tom_roush.pdfbox.pdmodel.graphics.color.PDColorSpace;
import com.tom_roush.pdfbox.pdmodel.graphics.form.PDFormXObject;
import com.tom_roush.pdfbox.pdmodel.graphics.pattern.PDTilingPattern;
import com.tom_roush.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import com.tom_roush.pdfbox.pdmodel.graphics.state.PDTextState;
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import com.tom_roush.pdfbox.util.Matrix;
import com.tom_roush.pdfbox.util.Vector;

/**
 * Processes a PDF content stream and executes certain operations.
 * Provides a callback interface for clients that want to do things with the stream.
 *
 * @author Ben Litchfield
 */
public abstract class PDFStreamEngine
{
    private final Map<String, OperatorProcessor> operators = new HashMap<String, OperatorProcessor>();

    private Matrix textMatrix;
    private Matrix textLineMatrix;

    private Stack<PDGraphicsState> graphicsStack = new Stack<PDGraphicsState>();

    private PDResources resources;
    private PDPage currentPage;
    private boolean isProcessingPage;
    private Matrix initialMatrix;

    /**
     * Creates a new PDFStreamEngine.
     */
    protected PDFStreamEngine()
    {
    }

    /**
     * Register a custom operator processor with the engine.
     *
     * @param operator The operator as a string.
     * @param op Processor instance.
     * @deprecated Use {@link #addOperator(OperatorProcessor)} instead
     */
    @Deprecated
    public void registerOperatorProcessor(String operator, OperatorProcessor op)
    {
        op.setContext(this);
        operators.put(operator, op);
    }

    /**
     * Adds an operator processor to the engine.
     *
     * @param op operator processor
     */
    public final void addOperator(OperatorProcessor op)
    {
        op.setContext(this);
        operators.put(op.getName(), op);
    }

    /**
     * Initialises the stream engine for the given page.
     */
    private void initPage(PDPage page)
    {
        if (page == null)
        {
            throw new IllegalArgumentException("Page cannot be null");
        }
        currentPage = page;
        graphicsStack.clear();
        graphicsStack.push(new PDGraphicsState(page.getCropBox()));
        textMatrix = null;
        textLineMatrix = null;
        resources = null;
        initialMatrix = page.getMatrix();
    }

    /**
     * This will initialise and process the contents of the stream.
     *
     * @param page the page to process
     * @throws IOException if there is an error accessing the stream
     */
    public void processPage(PDPage page) throws IOException
    {
        initPage(page);
        if (page.hasContents())
        {
            isProcessingPage = true;
            processStream(page);
            isProcessingPage = false;
        }
    }

    /**
     * Shows a transparency group from the content stream.
     *
     * @param form transparency group (form) XObject
     * @throws IOException if the transparency group cannot be processed
     */
    public void showTransparencyGroup(PDFormXObject form) throws IOException
    {
        processTransparencyGroup(form);
    }

    /**
     * Shows a form from the content stream.
     *
     * @param form form XObject
     * @throws IOException if the form cannot be processed
     */
    public void showForm(PDFormXObject form) throws IOException
    {
        if (currentPage == null)
        {
            throw new IllegalStateException("No current page, call " +
                "#processChildStream(PDContentStream, PDPage) instead");
        }
        processStream(form);
    }

    /**
     * Processes a soft mask transparency group stream.
     */
    protected void processSoftMask(PDFormXObject group) throws IOException
    {
        // clear the current soft mask (this mask) to avoid recursion
        Stack<PDGraphicsState> savedStack = saveGraphicsStack();
        getGraphicsState().setSoftMask(null);
        processTransparencyGroup(group);
        restoreGraphicsStack(savedStack);
    }

    /**
     * Processes a transparency group stream.
     */
    protected void processTransparencyGroup(PDFormXObject group) throws IOException
    {
        if (currentPage == null)
        {
            throw new IllegalStateException("No current page, call " +
                "#processChildStream(PDContentStream, PDPage) instead");
        }

        PDResources parent = pushResources(group);
        Stack<PDGraphicsState> savedStack = saveGraphicsStack();

        // transform the CTM using the stream's matrix
        getGraphicsState().getCurrentTransformationMatrix().concatenate(group.getMatrix());

        // note: we don't clip to the BBox as it is often wrong, see PDFBOX-1917

        processStreamOperators(group);

        restoreGraphicsStack(savedStack);
        popResources(parent);
    }

    /**
     * Processes a Type 3 character stream.
     *
     * @param charProc Type 3 character procedure
     * @param textRenderingMatrix the Text Rendering Matrix
     */
    protected void processType3Stream(PDType3CharProc charProc, Matrix textRenderingMatrix)
        throws IOException
    {
        if (currentPage == null)
        {
            throw new IllegalStateException("No current page, call " +
                "#processChildStream(PDContentStream, PDPage) instead");
        }

        PDResources parent = pushResources(charProc);
        saveGraphicsState();

        // replace the CTM with the TRM
        getGraphicsState().setCurrentTransformationMatrix(textRenderingMatrix);

        // transform the CTM using the stream's matrix (this is the FontMatrix)
        getGraphicsState().getCurrentTransformationMatrix().concatenate(charProc.getMatrix());

        // note: we don't clip to the BBox as it is often wrong, see PDFBOX-1917

        // save text matrices (Type 3 stream may contain BT/ET, see PDFBOX-2137)
        Matrix textMatrixOld = textMatrix;
        textMatrix = new Matrix();
        Matrix textLineMatrixOld = textLineMatrix;
        textLineMatrix = new Matrix();

        processStreamOperators(charProc);

        // restore text matrices
        textMatrix = textMatrixOld;
        textLineMatrix = textLineMatrixOld;

        restoreGraphicsState();
        popResources(parent);
    }

    /**
     * Process the given annotation with the specified appearance stream.
     *
     * @param annotation The annotation containing the appearance stream to process.
     * @param appearance The appearance stream to process.
     */
    protected void processAnnotation(PDAnnotation annotation, PDAppearanceStream appearance)
        throws IOException
    {
        PDResources parent = pushResources(appearance);
        saveGraphicsState();

        PDRectangle bbox = appearance.getBBox();
        PDRectangle rect = annotation.getRectangle();
        Matrix matrix = appearance.getMatrix();

        // zero-sized rectangles are not valid
        if (rect.getWidth() > 0 && rect.getHeight() > 0)
        {
            // transformed appearance box fixme: may be an arbitrary shape
            RectF transformedBox = new RectF();
            bbox.transform(matrix).computeBounds(transformedBox, true);

            // compute a matrix which scales and translates the transformed appearance box to align
            // with the edges of the annotation's rectangle
            Matrix a = Matrix.getTranslateInstance(rect.getLowerLeftX(), rect.getLowerLeftY());
            a.concatenate(Matrix.getScaleInstance(rect.getWidth() / transformedBox.width(),
                rect.getHeight() / transformedBox.height()));
            a.concatenate(Matrix.getTranslateInstance(-transformedBox.left,
                -transformedBox.top));

            // Matrix shall be concatenated with A to form a matrix AA that maps from the appearance's
            // coordinate system to the annotation's rectangle in default user space
            Matrix aa = Matrix.concatenate(matrix, a);

            // make matrix AA the CTM
            getGraphicsState().setCurrentTransformationMatrix(aa);

            // clip to bounding box
            clipToRect(bbox);

            processStreamOperators(appearance);
        }

        restoreGraphicsState();
        popResources(parent);
    }


    /**
     * Process the given tiling pattern.
     *
     * @param tilingPattern the tiling pattern
     * @param color color to use, if this is an uncoloured pattern, otherwise null.
     * @param colorSpace color space to use, if this is an uncoloured pattern, otherwise null.
     */
    protected final void processTilingPattern(PDTilingPattern tilingPattern, PDColor color,
        PDColorSpace colorSpace) throws IOException
    {
        processTilingPattern(tilingPattern, color, colorSpace, tilingPattern.getMatrix());
    }

    /**
     * Process the given tiling pattern. Allows the pattern matrix to be overridden for custom
     * rendering.
     *
     * @param tilingPattern the tiling pattern
     * @param color color to use, if this is an uncoloured pattern, otherwise null.
     * @param colorSpace color space to use, if this is an uncoloured pattern, otherwise null.
     * @param patternMatrix the pattern matrix, may be overridden for custom rendering.
     */
    protected final void processTilingPattern(PDTilingPattern tilingPattern, PDColor color,
        PDColorSpace colorSpace, Matrix patternMatrix)
        throws IOException
    {
        PDResources parent = pushResources(tilingPattern);

        Matrix parentMatrix = initialMatrix;
        initialMatrix = Matrix.concatenate(initialMatrix, patternMatrix);

        // save the original graphics state
        Stack<PDGraphicsState> savedStack = saveGraphicsStack();

        // save a clean state (new clipping path, line path, etc.)
        RectF bbox = new RectF();
        tilingPattern.getBBox().transform(patternMatrix).computeBounds(bbox, true);
        PDRectangle rect = new PDRectangle(bbox.left, bbox.top,
            bbox.width(), bbox.height());
        graphicsStack.push(new PDGraphicsState(rect));

        // non-colored patterns have to be given a color
        if (colorSpace != null)
        {
            color = new PDColor(color.getComponents(), colorSpace);
            getGraphicsState().setNonStrokingColorSpace(colorSpace);
            getGraphicsState().setNonStrokingColor(color);
            getGraphicsState().setStrokingColorSpace(colorSpace);
            getGraphicsState().setStrokingColor(color);
        }

        // transform the CTM using the stream's matrix
        getGraphicsState().getCurrentTransformationMatrix().concatenate(patternMatrix);

        // clip to bounding box
        clipToRect(tilingPattern.getBBox());

        processStreamOperators(tilingPattern);

        initialMatrix = parentMatrix;
        restoreGraphicsStack(savedStack);
        popResources(parent);
    }

    /**
     * Shows the given annotation.
     *
     * @param annotation An annotation on the current page.
     * @throws IOException If an error occurred reading the annotation
     */
    public void showAnnotation(PDAnnotation annotation) throws IOException
    {
        PDAppearanceStream appearanceStream = getAppearance(annotation);
        if (appearanceStream != null)
        {
            processAnnotation(annotation, appearanceStream);
        }
    }

    /**
     * Returns the appearance stream to process for the given annotation. May be used to render
     * a specific appearance such as "hover".
     *
     * @param annotation The current annotation.
     * @return The stream to process.
     */
    public PDAppearanceStream getAppearance(PDAnnotation annotation)
    {
        return annotation.getNormalAppearanceStream();
    }

    /**
     * Process a child stream of the given page. Cannot be used with #processPage(PDPage).
     *
     * @param contentStream the child content stream
     * @throws IOException if there is an exception while processing the stream
     */
    protected void processChildStream(PDContentStream contentStream, PDPage page) throws IOException
    {
        if (isProcessingPage)
        {
            throw new IllegalStateException("Current page has already been set via " +
                " #processPage(PDPage) call #processChildStream(PDContentStream) instead");
        }
        initPage(page);
        processStream(contentStream);
        currentPage = null;
    }

    /**
     * Process a content stream.
     *
     * @param contentStream the content stream
     * @throws IOException if there is an exception while processing the stream
     */
    private void processStream(PDContentStream contentStream) throws IOException
    {
        PDResources parent = pushResources(contentStream);
        Stack<PDGraphicsState> savedStack = saveGraphicsStack();
        Matrix parentMatrix = initialMatrix;

        // transform the CTM using the stream's matrix
        getGraphicsState().getCurrentTransformationMatrix().concatenate(contentStream.getMatrix());

        // the stream's initial matrix includes the parent CTM, e.g. this allows a scaled form
        initialMatrix = getGraphicsState().getCurrentTransformationMatrix().clone();

        // clip to bounding box
        PDRectangle bbox = contentStream.getBBox();
        clipToRect(bbox);

        processStreamOperators(contentStream);

        initialMatrix = parentMatrix;
        restoreGraphicsStack(savedStack);
        popResources(parent);
    }

    /**
     * Processes the operators of the given content stream.
     */
    private void processStreamOperators(PDContentStream contentStream) throws IOException
    {
        List<COSBase> arguments = new ArrayList<COSBase>();
        PDFStreamParser parser = new PDFStreamParser(contentStream);
        Object token = parser.parseNextToken();
        while (token != null)
        {
            if (token instanceof COSObject)
            {
                arguments.add(((COSObject) token).getObject());
            }
            else if (token instanceof Operator)
            {
                processOperator((Operator) token, arguments);
                arguments = new ArrayList<COSBase>();
            }
            else
            {
                arguments.add((COSBase) token);
            }
            token = parser.parseNextToken();
        }
    }

    /**
     * Pushes the given stream's resources, returning the previous resources.
     */
    private PDResources pushResources(PDContentStream contentStream)
    {
        // resource lookup: first look for stream resources, then fallback to the current page
        PDResources parentResources = resources;
        PDResources streamResources = contentStream.getResources();
        if (streamResources != null)
        {
            resources = streamResources;
        }
        else if (resources != null)
        {
            // inherit directly from parent stream, this is not in the PDF spec, but the file from
            // PDFBOX-1359 does this and works in Acrobat
        }
        else
        {
            resources = currentPage.getResources();
        }

        // resources are required in PDF
        if (resources == null)
        {
            resources = new PDResources();
        }
        return parentResources;
    }

    /**
     * Pops the current resources, replacing them with the given resources.
     */
    private void popResources(PDResources parentResources)
    {
        resources = parentResources;
    }

    /**
     * Transforms the given rectangle using the CTM and then intersects it with the current
     * clipping area.
     */
    private void clipToRect(PDRectangle rectangle)
    {
        if (rectangle != null)
        {
            Path clip = rectangle.transform(getGraphicsState().getCurrentTransformationMatrix());
            getGraphicsState().intersectClippingPath(clip);
        }
    }

    /**
     * Called when the BT operator is encountered. This method is for overriding in subclasses, the
     * default implementation does nothing.
     *
     * @throws IOException if there was an error processing the text
     */
    public void beginText() throws IOException
    {
        // overridden in subclasses
    }

    /**
     * Called when the ET operator is encountered. This method is for overriding in subclasses, the
     * default implementation does nothing.
     *
     * @throws IOException if there was an error processing the text
     */
    public void endText() throws IOException
    {
        // overridden in subclasses
    }

    /**
     * Called when a string of text is to be shown.
     *
     * @param string the encoded text
     * @throws IOException if there was an error showing the text
     */
    public void showTextString(byte[] string) throws IOException
    {
        showText(string);
    }

    /**
     * Called when a string of text with spacing adjustments is to be shown.
     *
     * @param array array of encoded text strings and adjustments
     * @throws IOException if there was an error showing the text
     */
    public void showTextStrings(COSArray array) throws IOException
    {
        PDTextState textState = getGraphicsState().getTextState();
        float fontSize = textState.getFontSize();
        float horizontalScaling = textState.getHorizontalScaling() / 100f;
        boolean isVertical = textState.getFont().isVertical();

        for (COSBase obj : array)
        {
            if (obj instanceof COSNumber)
            {
                float tj = ((COSNumber)obj).floatValue();

                // calculate the combined displacements
                float tx, ty;
                if (isVertical)
                {
                    tx = 0;
                    ty = -tj / 1000 * fontSize;
                }
                else
                {
                    tx = -tj / 1000 * fontSize * horizontalScaling;
                    ty = 0;
                }

                applyTextAdjustment(tx, ty);
            }
            else if(obj instanceof COSString)
            {
                byte[] string = ((COSString)obj).getBytes();
                showText(string);
            }
            else
            {
                throw new IOException("Unknown type in array for TJ operation:" + obj);
            }
        }
    }

    /**
     * Applies a text position adjustment from the TJ operator. May be overridden in subclasses.
     *
     * @param tx x-translation
     * @param ty y-translation
     */
    protected void applyTextAdjustment(float tx, float ty) throws IOException
    {
        // update the text matrix
        textMatrix.concatenate(Matrix.getTranslateInstance(tx, ty));
    }

    /**
     * Process text from the PDF Stream. You should override this method if you want to
     * perform an action when encoded text is being processed.
     *
     * @param string the encoded text
     * @throws IOException if there is an error processing the string
     */
    protected void showText(byte[] string) throws IOException
    {
        PDGraphicsState state = getGraphicsState();
        PDTextState textState = state.getTextState();

        // get the current font
        PDFont font = textState.getFont();
        if (font == null)
        {
            Log.w("PdfBox-Android", "No current font, will use default");
            font = PDFontFactory.createDefaultFont();
        }

        float fontSize = textState.getFontSize();
        float horizontalScaling = textState.getHorizontalScaling() / 100f;
        float charSpacing = textState.getCharacterSpacing();

        // put the text state parameters into matrix form
        Matrix parameters = new Matrix(
            fontSize * horizontalScaling, 0, // 0
            0, fontSize,                     // 0
            0, textState.getRise());         // 1

        // read the stream until it is empty
        InputStream in = new ByteArrayInputStream(string);
        while (in.available() > 0)
        {
            // decode a character
            int before = in.available();
            int code = font.readCode(in);
            int codeLength = before - in.available();
            String unicode = font.toUnicode(code);

            // Word spacing shall be applied to every occurrence of the single-byte character code
            // 32 in a string when using a simple font or a composite font that defines code 32 as
            // a single-byte code.
            float wordSpacing = 0;
            if (codeLength == 1 && code == 32)
            {
                wordSpacing += textState.getWordSpacing();
            }

            // text rendering matrix (text space -> device space)
            Matrix ctm = state.getCurrentTransformationMatrix();
            Matrix textRenderingMatrix = parameters.multiply(textMatrix).multiply(ctm);

            // get glyph's position vector if this is vertical text
            // changes to vertical text should be tested with PDFBOX-2294 and PDFBOX-1422
            if (font.isVertical())
            {
                // position vector, in text space
                Vector v = font.getPositionVector(code);

                // apply the position vector to the horizontal origin to get the vertical origin
                textRenderingMatrix.translate(v);
            }

            // get glyph's horizontal and vertical displacements, in text space
            Vector w = font.getDisplacement(code);

            // process the decoded glyph
            saveGraphicsState();
            Matrix textMatrixOld = textMatrix;
            Matrix textLineMatrixOld = textLineMatrix;
            showGlyph(textRenderingMatrix, font, code, unicode, w);
            textMatrix = textMatrixOld;
            textLineMatrix = textLineMatrixOld;
            restoreGraphicsState();

            // calculate the combined displacements
            float tx, ty;
            if (font.isVertical())
            {
                tx = 0;
                ty = w.getY() * fontSize + charSpacing + wordSpacing;
            }
            else
            {
                tx = (w.getX() * fontSize + charSpacing + wordSpacing) * horizontalScaling;
                ty = 0;
            }

            // update the text matrix
            textMatrix.concatenate(Matrix.getTranslateInstance(tx, ty));
        }
    }

    /**
     * Called when a glyph is to be processed.This method is intended for overriding in subclasses,
     * the default implementation does nothing.
     *
     * @param textRenderingMatrix the current text rendering matrix, T<sub>rm</sub>
     * @param font the current font
     * @param code internal PDF character code for the glyph
     * @param unicode the Unicode text for this glyph, or null if the PDF does provide it
     * @param displacement the displacement (i.e. advance) of the glyph in text space
     * @throws IOException if the glyph cannot be processed
     */
    protected void showGlyph(Matrix textRenderingMatrix, PDFont font, int code, String unicode,
        Vector displacement) throws IOException
    {
        if (font instanceof PDType3Font)
        {
            showType3Glyph(textRenderingMatrix, (PDType3Font)font, code, unicode, displacement);
        }
        else
        {
            showFontGlyph(textRenderingMatrix, font, code, unicode, displacement);
        }
    }

    /**
     * Called when a glyph is to be processed.This method is intended for overriding in subclasses,
     * the default implementation does nothing.
     *
     * @param textRenderingMatrix the current text rendering matrix, T<sub>rm</sub>
     * @param font the current font
     * @param code internal PDF character code for the glyph
     * @param unicode the Unicode text for this glyph, or null if the PDF does provide it
     * @param displacement the displacement (i.e. advance) of the glyph in text space
     * @throws IOException if the glyph cannot be processed
     */
    protected void showFontGlyph(Matrix textRenderingMatrix, PDFont font, int code, String unicode,
        Vector displacement) throws IOException
    {
        // overridden in subclasses
    }

    /**
     * Called when a glyph is to be processed.This method is intended for overriding in subclasses,
     * the default implementation does nothing.
     *
     * @param textRenderingMatrix the current text rendering matrix, T<sub>rm</sub>
     * @param font the current font
     * @param code internal PDF character code for the glyph
     * @param unicode the Unicode text for this glyph, or null if the PDF does provide it
     * @param displacement the displacement (i.e. advance) of the glyph in text space
     * @throws IOException if the glyph cannot be processed
     */
    protected void showType3Glyph(Matrix textRenderingMatrix, PDType3Font font, int code,
        String unicode, Vector displacement) throws IOException
    {
        PDType3CharProc charProc = font.getCharProc(code);
        if (charProc != null)
        {
            processType3Stream(charProc, textRenderingMatrix);
        }
    }

    /**
     * This is used to handle an operation.
     *
     * @param operation The operation to perform.
     * @param arguments The list of arguments.
     * @throws IOException If there is an error processing the operation.
     */
    public void processOperator(String operation, List<COSBase> arguments) throws IOException
    {
        Operator operator = Operator.getOperator(operation);
        processOperator(operator, arguments);
    }

    /**
     * This is used to handle an operation.
     *
     * @param operator The operation to perform.
     * @param operands The list of arguments.
     * @throws IOException If there is an error processing the operation.
     */
    protected void processOperator(Operator operator, List<COSBase> operands) throws IOException
    {
        String name = operator.getName();
        OperatorProcessor processor = operators.get(name);
        if (processor != null)
        {
            processor.setContext(this);
            try
            {
                processor.process(operator, operands);
            }
            catch (IOException e)
            {
                operatorException(operator, operands, e);
            }
        }
        else
        {
            unsupportedOperator(operator, operands);
        }
    }

    /**
     * Called when an unsupported operator is encountered.
     *
     * @param operator The unknown operator.
     * @param operands The list of arguments.
     */
    protected void unsupportedOperator(Operator operator, List<COSBase> operands) throws IOException
    {
        // overridden in subclasses
    }

    /**
     * Called when an exception is thrown by an operator.
     *
     * @param operator The unknown operator.
     * @param operands The list of operands.
     */
    protected void operatorException(Operator operator, List<COSBase> operands, IOException e)
        throws IOException
    {
        if (e instanceof MissingOperandException ||
            e instanceof MissingResourceException ||
            e instanceof MissingImageReaderException)
        {
            Log.e("PdfBox-Android", e.getMessage());
        }
        else if (e instanceof EmptyGraphicsStackException)
        {
            Log.w("PdfBox-Android", e.getMessage());
        }
        else if (operator.getName().equals("Do"))
        {
            // todo: this too forgiving, but PDFBox has always worked this way for DrawObject
            // some careful refactoring is needed
            Log.w("PdfBox-Android", e.getMessage());
        }
        else
        {
            throw e;
        }
    }

    /**
     * Pushes the current graphics state to the stack.
     */
    public void saveGraphicsState()
    {
        graphicsStack.push(graphicsStack.peek().clone());
    }

    /**
     * Pops the current graphics state from the stack.
     */
    public void restoreGraphicsState()
    {
        graphicsStack.pop();
    }

    /**
     * Saves the entire graphics stack.
     */
    protected final Stack<PDGraphicsState> saveGraphicsStack()
    {
        Stack<PDGraphicsState> savedStack = graphicsStack;
        graphicsStack = new Stack<PDGraphicsState>();
        graphicsStack.add(savedStack.peek().clone());
        return savedStack;
    }

    /**
     * Restores the entire graphics stack.
     */
    protected final void restoreGraphicsStack(Stack<PDGraphicsState> snapshot)
    {
        graphicsStack = snapshot;
    }

    /**
     * @return Returns the size of the graphicsStack.
     */
    public int getGraphicsStackSize()
    {
        return graphicsStack.size();
    }

    /**
     * @return Returns the graphicsState.
     */
    public PDGraphicsState getGraphicsState()
    {
        return graphicsStack.peek();
    }

    /**
     * @return Returns the textLineMatrix.
     */
    public Matrix getTextLineMatrix()
    {
        return textLineMatrix;
    }

    /**
     * @param value The textLineMatrix to set.
     */
    public void setTextLineMatrix(Matrix value)
    {
        textLineMatrix = value;
    }

    /**
     * @return Returns the textMatrix.
     */
    public Matrix getTextMatrix()
    {
        return textMatrix;
    }

    /**
     * @param value The textMatrix to set.
     */
    public void setTextMatrix(Matrix value)
    {
        textMatrix = value;
    }

    /**
     * @param array dash array
     * @param phase dash phase
     */
    public void setLineDashPattern(COSArray array, int phase)
    {
        if (phase < 0)
        {
            Log.w("PdfBox-Android", "Dash phase has negative value " + phase + ", set to 0");
            phase = 0;
        }
        PDLineDashPattern lineDash = new PDLineDashPattern(array, phase);
        getGraphicsState().setLineDashPattern(lineDash);
    }

    /**
     * Returns the stream' resources.
     */
    public PDResources getResources()
    {
        return resources;
    }

    /**
     * Returns the current page.
     */
    public PDPage getCurrentPage()
    {
        return currentPage;
    }

    /**
     * Gets the stream's initial matrix.
     */
    public Matrix getInitialMatrix()
    {
        return initialMatrix;
    }

    /**
     * Transforms a point using the CTM.
     */
    public PointF transformedPoint(float x, float y)
    {
        float[] position = { x, y };
        getGraphicsState().getCurrentTransformationMatrix().createAffineTransform().transform(position, 0, position, 0, 1);
        return new PointF(position[0], position[1]);
    }

    // transforms a width using the CTM
    protected float transformWidth(float width)
    {
        Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
        float x = ctm.getScaleX() + ctm.getShearX();
        float y = ctm.getScaleY() + ctm.getShearY();
        return width * (float)Math.sqrt((x * x + y * y) * 0.5);
    }
}
