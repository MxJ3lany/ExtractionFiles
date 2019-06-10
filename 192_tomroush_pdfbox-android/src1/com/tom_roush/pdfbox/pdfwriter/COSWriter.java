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
package com.tom_roush.pdfbox.pdfwriter;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.tom_roush.pdfbox.cos.COSArray;
import com.tom_roush.pdfbox.cos.COSBase;
import com.tom_roush.pdfbox.cos.COSBoolean;
import com.tom_roush.pdfbox.cos.COSDictionary;
import com.tom_roush.pdfbox.cos.COSDocument;
import com.tom_roush.pdfbox.cos.COSFloat;
import com.tom_roush.pdfbox.cos.COSInteger;
import com.tom_roush.pdfbox.cos.COSName;
import com.tom_roush.pdfbox.cos.COSNull;
import com.tom_roush.pdfbox.cos.COSNumber;
import com.tom_roush.pdfbox.cos.COSObject;
import com.tom_roush.pdfbox.cos.COSObjectKey;
import com.tom_roush.pdfbox.cos.COSStream;
import com.tom_roush.pdfbox.cos.COSString;
import com.tom_roush.pdfbox.cos.COSUpdateInfo;
import com.tom_roush.pdfbox.cos.ICOSVisitor;
import com.tom_roush.pdfbox.io.IOUtils;
import com.tom_roush.pdfbox.pdfparser.PDFXRefStream;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.encryption.SecurityHandler;
import com.tom_roush.pdfbox.pdmodel.fdf.FDFDocument;
import com.tom_roush.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import com.tom_roush.pdfbox.util.Charsets;
import com.tom_roush.pdfbox.util.Hex;

/**
 * This class acts on a in-memory representation of a PDF document.
 *
 * @author Michael Traut
 * @author Ben Litchfield
 */
public class COSWriter implements ICOSVisitor, Closeable
{
    /**
     * The dictionary open token.
     */
    public static final byte[] DICT_OPEN = "<<".getBytes(Charsets.US_ASCII);
    /**
     * The dictionary close token.
     */
    public static final byte[] DICT_CLOSE = ">>".getBytes(Charsets.US_ASCII);
    /**
     * space character.
     */
    public static final byte[] SPACE = { ' ' };
    /**
     * The start to a PDF comment.
     */
    public static final byte[] COMMENT = { '%' };

    /**
     * The output version of the PDF.
     */
    public static final byte[] VERSION = "PDF-1.4".getBytes(Charsets.US_ASCII);
    /**
     * Garbage bytes used to create the PDF header.
     */
    public static final byte[] GARBAGE = new byte[] {(byte)0xf6, (byte)0xe4, (byte)0xfc, (byte)0xdf};
    /**
     * The EOF constant.
     */
    public static final byte[] EOF = "%%EOF".getBytes(Charsets.US_ASCII);
    // pdf tokens

    /**
     * The reference token.
     */
    public static final byte[] REFERENCE = "R".getBytes(Charsets.US_ASCII);
    /**
     * The XREF token.
     */
    public static final byte[] XREF = "xref".getBytes(Charsets.US_ASCII);
    /**
     * The xref free token.
     */
    public static final byte[] XREF_FREE = "f".getBytes(Charsets.US_ASCII);
    /**
     * The xref used token.
     */
    public static final byte[] XREF_USED = "n".getBytes(Charsets.US_ASCII);
    /**
     * The trailer token.
     */
    public static final byte[] TRAILER = "trailer".getBytes(Charsets.US_ASCII);
    /**
     * The start xref token.
     */
    public static final byte[] STARTXREF = "startxref".getBytes(Charsets.US_ASCII);
    /**
     * The starting object token.
     */
    public static final byte[] OBJ = "obj".getBytes(Charsets.US_ASCII);
    /**
     * The end object token.
     */
    public static final byte[] ENDOBJ = "endobj".getBytes(Charsets.US_ASCII);
    /**
     * The array open token.
     */
    public static final byte[] ARRAY_OPEN = "[".getBytes(Charsets.US_ASCII);
    /**
     * The array close token.
     */
    public static final byte[] ARRAY_CLOSE = "]".getBytes(Charsets.US_ASCII);
    /**
     * The open stream token.
     */
    public static final byte[] STREAM = "stream".getBytes(Charsets.US_ASCII);
    /**
     * The close stream token.
     */
    public static final byte[] ENDSTREAM = "endstream".getBytes(Charsets.US_ASCII);

    private final NumberFormat formatXrefOffset = new DecimalFormat("0000000000",
        DecimalFormatSymbols.getInstance(Locale.US));

    // the decimal format for the xref object generation number data
    private final NumberFormat formatXrefGeneration = new DecimalFormat("00000",
        DecimalFormatSymbols.getInstance(Locale.US));

    private final NumberFormat formatDecimal = NumberFormat.getNumberInstance( Locale.US );

    // the stream where we create the pdf output
    private OutputStream output;

    // the stream used to write standard cos data
    private COSStandardOutputStream standardOutput;

    // the start position of the x ref section
    private long startxref = 0;

    // the current object number
    private long number = 0;

    // maps the object to the keys generated in the writer
    // these are used for indirect references in other objects
    //A hashtable is used on purpose over a hashmap
    //so that null entries will not get added.
    private final Map<COSBase,COSObjectKey> objectKeys = new Hashtable<COSBase,COSObjectKey>();
    private final Map<COSObjectKey,COSBase> keyObject = new Hashtable<COSObjectKey,COSBase>();

    // the list of x ref entries to be made so far
    private final List<COSWriterXRefEntry> xRefEntries = new ArrayList<COSWriterXRefEntry>();
    private final Set<COSBase> objectsToWriteSet = new HashSet<COSBase>();

    //A list of objects to write.
    private final Deque<COSBase> objectsToWrite = new LinkedList<COSBase>();

    //a list of objects already written
    private final Set<COSBase> writtenObjects = new HashSet<COSBase>();

    //An 'actual' is any COSBase that is not a COSObject.
    //need to keep a list of the actuals that are added
    //as well as the objects because there is a problem
    //when adding a COSObject and then later adding
    //the actual for that object, so we will track
    //actuals separately.
    private final Set<COSBase> actualsAdded = new HashSet<COSBase>();

    private COSObjectKey currentObjectKey = null;
    private PDDocument pdDocument = null;
    private FDFDocument fdfDocument = null;
    private boolean willEncrypt = false;

    // signing
    private boolean incrementalUpdate = false;
    private boolean reachedSignature = false;
    private long signatureOffset, signatureLength;
    private long byteRangeOffset, byteRangeLength;
    private InputStream incrementalInput;
    private OutputStream incrementalOutput;
    private SignatureInterface signatureInterface;

    /**
     * COSWriter constructor comment.
     *
     * @param os The wrapped output stream.
     */
    public COSWriter(OutputStream os)
    {
        super();
        setOutput(os);
        setStandardOutput(new COSStandardOutputStream(output));
        formatDecimal.setMaximumFractionDigits( 10 );
        formatDecimal.setGroupingUsed( false );
    }

    /**
     * COSWriter constructor for incremental updates.
     *
     * @param outputStream output stream where the new PDF data will be written
     * @param inputStream input stream containing source PDF data
     *
     * @throws IOException if something went wrong
     */
    public COSWriter(OutputStream outputStream, InputStream inputStream) throws IOException
    {
        super();

        // write to buffer instead of output
        setOutput(new ByteArrayOutputStream());
        setStandardOutput(new COSStandardOutputStream(output, inputStream.available()));

        incrementalInput = inputStream;
        incrementalOutput = outputStream;
        incrementalUpdate = true;

        formatDecimal.setMaximumFractionDigits( 10 );
        formatDecimal.setGroupingUsed( false );
    }

    private void prepareIncrement(PDDocument doc)
    {
        try
        {
            if (doc != null)
            {
                COSDocument cosDoc = doc.getDocument();

                Map<COSObjectKey, Long> xrefTable = cosDoc.getXrefTable();
                Set<COSObjectKey> keySet = xrefTable.keySet();
                long highestNumber=0;
                for ( COSObjectKey cosObjectKey : keySet )
                {
                    COSBase object = cosDoc.getObjectFromPool(cosObjectKey).getObject();
                    if (object != null && cosObjectKey!= null && !(object instanceof COSNumber))
                    {
                        objectKeys.put(object, cosObjectKey);
                        keyObject.put(cosObjectKey,object);
                    }

                    if (cosObjectKey != null)
                    {
                        long num = cosObjectKey.getNumber();
                        if (num > highestNumber)
                        {
                            highestNumber = num;
                        }
                    }
                }
                setNumber(highestNumber);
            }
        }
        catch (IOException e)
        {
            Log.e("PdfBox-Android", e.getMessage(),e);
        }
    }

    /**
     * add an entry in the x ref table for later dump.
     *
     * @param entry The new entry to add.
     */
    protected void addXRefEntry(COSWriterXRefEntry entry)
    {
        getXRefEntries().add(entry);
    }

    /**
     * This will close the stream.
     *
     * @throws IOException If the underlying stream throws an exception.
     */
    @Override
    public void close() throws IOException
    {
        if (getStandardOutput() != null)
        {
            getStandardOutput().close();
        }
        if (getOutput() != null)
        {
            getOutput().close();
        }
        if (incrementalOutput != null)
        {
            incrementalOutput.close();
        }
    }

    /**
     * This will get the current object number.
     *
     * @return The current object number.
     */
    protected long getNumber()
    {
        return number;
    }

    /**
     * This will get all available object keys.
     *
     * @return A map of all object keys.
     */
    public Map<COSBase,COSObjectKey> getObjectKeys()
    {
        return objectKeys;
    }

    /**
     * This will get the output stream.
     *
     * @return The output stream.
     */
    protected java.io.OutputStream getOutput()
    {
        return output;
    }

    /**
     * This will get the standard output stream.
     *
     * @return The standard output stream.
     */
    protected COSStandardOutputStream getStandardOutput()
    {
        return standardOutput;
    }

    /**
     * This will get the current start xref.
     *
     * @return The current start xref.
     */
    protected long getStartxref()
    {
        return startxref;
    }
    /**
     * This will get the xref entries.
     *
     * @return All available xref entries.
     */
    protected List<COSWriterXRefEntry> getXRefEntries()
    {
        return xRefEntries;
    }

    /**
     * This will set the current object number.
     *
     * @param newNumber The new object number.
     */
    protected void setNumber(long newNumber)
    {
        number = newNumber;
    }

    /**
     * This will set the output stream.
     *
     * @param newOutput The new output stream.
     */
    private void setOutput( OutputStream newOutput )
    {
        output = newOutput;
    }

    /**
     * This will set the standard output stream.
     *
     * @param newStandardOutput The new standard output stream.
     */
    private void setStandardOutput(COSStandardOutputStream newStandardOutput)
    {
        standardOutput = newStandardOutput;
    }

    /**
     * This will set the start xref.
     *
     * @param newStartxref The new start xref attribute.
     */
    protected void setStartxref(long newStartxref)
    {
        startxref = newStartxref;
    }

    /**
     * This will write the body of the document.
     *
     * @param doc The document to write the body for.
     *
     * @throws IOException If there is an error writing the data.
     */
    protected void doWriteBody(COSDocument doc) throws IOException
    {
        COSDictionary trailer = doc.getTrailer();
        COSDictionary root = (COSDictionary)trailer.getDictionaryObject( COSName.ROOT );
        COSDictionary info = (COSDictionary)trailer.getDictionaryObject( COSName.INFO );
        COSDictionary encrypt = (COSDictionary)trailer.getDictionaryObject( COSName.ENCRYPT );
        if( root != null )
        {
            addObjectToWrite( root );
        }
        if( info != null )
        {
            addObjectToWrite( info );
        }

        while( objectsToWrite.size() > 0 )
        {
            COSBase nextObject = objectsToWrite.removeFirst();
            objectsToWriteSet.remove(nextObject);
            doWriteObject( nextObject );
        }
        willEncrypt = false;
        if( encrypt != null )
        {
            addObjectToWrite( encrypt );
        }

        while( objectsToWrite.size() > 0 )
        {
            COSBase nextObject = objectsToWrite.removeFirst();
            objectsToWriteSet.remove(nextObject);
            doWriteObject( nextObject );
        }
    }

    private void addObjectToWrite( COSBase object )
    {
        COSBase actual = object;
        if( actual instanceof COSObject )
        {
            actual = ((COSObject)actual).getObject();
        }

        if( !writtenObjects.contains( object ) &&
            !objectsToWriteSet.contains( object ) &&
            !actualsAdded.contains( actual ) )
        {
            COSBase cosBase=null;
            COSObjectKey cosObjectKey = null;
            if(actual != null)
            {
                cosObjectKey= objectKeys.get(actual);
            }
            if(cosObjectKey!=null)
            {
                cosBase = keyObject.get(cosObjectKey);
            }
            if (actual != null && objectKeys.containsKey(actual)
                && object instanceof COSUpdateInfo && !((COSUpdateInfo)object).isNeedToBeUpdated()
                && cosBase instanceof COSUpdateInfo && !((COSUpdateInfo)cosBase).isNeedToBeUpdated() )
            {
                return;
            }

            objectsToWrite.add( object );
            objectsToWriteSet.add( object );
            if( actual != null )
            {
                actualsAdded.add( actual );
            }
        }
    }

    /**
     * This will write a COS object.
     *
     * @param obj The object to write.
     *
     * @throws IOException if the output cannot be written
     */
    public void doWriteObject( COSBase obj ) throws IOException
    {
        writtenObjects.add( obj );
        if(obj instanceof COSDictionary)
        {
            COSDictionary dict = (COSDictionary)obj;
            COSBase itemType = dict.getItem(COSName.TYPE);
            if (itemType instanceof COSName)
            {
                COSName item = (COSName) itemType;
                if (COSName.SIG.equals(item) || COSName.DOC_TIME_STAMP.equals(item))
                {
                    reachedSignature = true;
                }
            }
        }

        // find the physical reference
        currentObjectKey = getObjectKey( obj );
        // add a x ref entry
        addXRefEntry( new COSWriterXRefEntry(getStandardOutput().getPos(), obj, currentObjectKey));
        // write the object
        getStandardOutput().write(String.valueOf(currentObjectKey.getNumber()).getBytes(Charsets.ISO_8859_1));
        getStandardOutput().write(SPACE);
        getStandardOutput().write(String.valueOf(currentObjectKey.getGeneration()).getBytes(Charsets.ISO_8859_1));
        getStandardOutput().write(SPACE);
        getStandardOutput().write(OBJ);
        getStandardOutput().writeEOL();
        obj.accept( this );
        getStandardOutput().writeEOL();
        getStandardOutput().write(ENDOBJ);
        getStandardOutput().writeEOL();
    }

    /**
     * This will write the header to the PDF document.
     *
     * @param doc The document to get the data from.
     *
     * @throws IOException If there is an error writing to the stream.
     */
    protected void doWriteHeader(COSDocument doc) throws IOException
    {
        String headerString;
        if (fdfDocument != null)
        {
            headerString = "%FDF-"+ Float.toString(fdfDocument.getDocument().getVersion());
        }
        else
        {
            headerString = "%PDF-"+ Float.toString(pdDocument.getDocument().getVersion());
        }
        getStandardOutput().write( headerString.getBytes(Charsets.ISO_8859_1) );

        getStandardOutput().writeEOL();
        getStandardOutput().write(COMMENT);
        getStandardOutput().write(GARBAGE);
        getStandardOutput().writeEOL();
    }


    /**
     * This will write the trailer to the PDF document.
     *
     * @param doc The document to create the trailer for.
     *
     * @throws IOException If there is an IOError while writing the document.
     */
    protected void doWriteTrailer(COSDocument doc) throws IOException
    {
        getStandardOutput().write(TRAILER);
        getStandardOutput().writeEOL();

        COSDictionary trailer = doc.getTrailer();
        //sort xref, needed only if object keys not regenerated
        Collections.sort(getXRefEntries());
        COSWriterXRefEntry lastEntry = getXRefEntries().get( getXRefEntries().size()-1);
        trailer.setLong(COSName.SIZE, lastEntry.getKey().getNumber()+1);
        // Only need to stay, if an incremental update will be performed
        if (!incrementalUpdate)
        {
            trailer.removeItem( COSName.PREV );
        }
        if (!doc.isXRefStream())
        {
            trailer.removeItem( COSName.XREF_STM );
        }
        // Remove a checksum if present
        trailer.removeItem( COSName.DOC_CHECKSUM );

        trailer.accept(this);
    }

    private void doWriteXRefInc(COSDocument doc, long hybridPrev) throws IOException
    {
        if (doc.isXRefStream() || hybridPrev != -1)
        {
            // the file uses XrefStreams, so we need to update
            // it with an xref stream. We create a new one and fill it
            // with data available here

            // create a new XRefStrema object
            PDFXRefStream pdfxRefStream = new PDFXRefStream();

            // add all entries from the incremental update.
            List<COSWriterXRefEntry> xRefEntries2 = getXRefEntries();
            for ( COSWriterXRefEntry cosWriterXRefEntry : xRefEntries2 )
            {
                pdfxRefStream.addEntry(cosWriterXRefEntry);
            }

            COSDictionary trailer = doc.getTrailer();
            if (incrementalUpdate)
            {
                // use previous startXref value as new PREV value
                trailer.setLong(COSName.PREV, doc.getStartXref());
            }
            else
            {
                trailer.removeItem(COSName.PREV);
            }
            pdfxRefStream.addTrailerInfo(trailer);
            // the size is the highest object number+1. we add one more
            // for the xref stream object we are going to write
            pdfxRefStream.setSize(getNumber() + 2);

            setStartxref(getStandardOutput().getPos());
            COSStream stream2 = pdfxRefStream.getStream();
            doWriteObject(stream2);
        }

        if (!doc.isXRefStream() || hybridPrev != -1)
        {
            COSDictionary trailer = doc.getTrailer();
            trailer.setLong(COSName.PREV, doc.getStartXref());
            if (hybridPrev != -1)
            {
                COSName xrefStm = COSName.XREF_STM;
                trailer.removeItem(xrefStm);
                trailer.setLong(xrefStm, getStartxref());
            }
            doWriteXRefTable();
            doWriteTrailer(doc);
        }
    }

    // writes the "xref" table
    private void doWriteXRefTable() throws IOException
    {
        addXRefEntry(COSWriterXRefEntry.getNullEntry());

        // sort xref, needed only if object keys not regenerated
        Collections.sort(getXRefEntries());

        // remember the position where x ref was written
        setStartxref(getStandardOutput().getPos());

        getStandardOutput().write(XREF);
        getStandardOutput().writeEOL();
        // write start object number and object count for this x ref section
        // we assume starting from scratch

        Long[] xRefRanges = getXRefRanges(getXRefEntries());
        int xRefLength = xRefRanges.length;
        int x = 0;
        int j = 0;
        while (x < xRefLength && (xRefLength % 2) == 0)
        {
            writeXrefRange(xRefRanges[x], xRefRanges[x + 1]);

            for (int i = 0; i < xRefRanges[x + 1]; ++i)
            {
                writeXrefEntry(xRefEntries.get(j++));
            }
            x += 2;
        }
    }

    private void doWriteSignature() throws IOException
    {
        if (signatureOffset == 0 || byteRangeOffset == 0)
        {
            return;
        }

        // calculate the ByteRange values
        long inLength = incrementalInput.available();
        long beforeLength = signatureOffset;
        long afterOffset = signatureOffset + signatureLength;
        long afterLength = getStandardOutput().getPos() - (inLength + signatureLength) - (signatureOffset - inLength);

        String byteRange = "0 " + beforeLength + " " + afterOffset + " " + afterLength + "]";
        if (byteRangeLength - byteRange.length() < 0)
        {
            throw new IOException("Can't write new ByteRange, not enough space");
        }

        // copy the new incremental data into a buffer (e.g. signature dict, trailer)
        ByteArrayOutputStream byteOut = (ByteArrayOutputStream) output;
        byteOut.flush();
        byte[] buffer = byteOut.toByteArray();

        // overwrite the ByteRange in the buffer
        byte[] byteRangeBytes = byteRange.getBytes();
        for (int i = 0; i < byteRangeLength; i++)
        {
            if (i >= byteRangeBytes.length)
            {
                buffer[(int)(byteRangeOffset + i - inLength)] = 0x20; // SPACE
            }
            else
            {
                buffer[(int)(byteRangeOffset + i - inLength)] = byteRangeBytes[i];
            }
        }

        // get the input PDF bytes
        byte[] inputBytes = IOUtils.toByteArray(incrementalInput);

        // get only the incremental bytes to be signed (includes /ByteRange but not /Contents)
        byte[] signBuffer = new byte[buffer.length - (int)signatureLength];
        int bufSignatureOffset = (int)(signatureOffset - inLength);
        System.arraycopy(buffer, 0, signBuffer, 0, bufSignatureOffset);
        System.arraycopy(buffer, bufSignatureOffset + (int)signatureLength,
            signBuffer, bufSignatureOffset, buffer.length - bufSignatureOffset - (int)signatureLength);

        SequenceInputStream signStream = new SequenceInputStream(new ByteArrayInputStream(inputBytes),
            new ByteArrayInputStream(signBuffer));

        // sign the bytes
        byte[] sign = signatureInterface.sign(signStream);
        String signature = new COSString(sign).toHexString();
        // substract 2 bytes because of the enclosing "<>"
        if (signature.length() > signatureLength - 2)
        {
            throw new IOException("Can't write signature, not enough space");
        }

        // overwrite the signature Contents in the buffer
        byte[] signatureBytes = signature.getBytes();
        System.arraycopy(signatureBytes, 0, buffer, bufSignatureOffset + 1, signatureBytes.length);

        // write the data to the incremental output stream
        incrementalOutput.write(inputBytes);
        incrementalOutput.write(buffer);
    }

    private void writeXrefRange(long x, long y) throws IOException
    {
        getStandardOutput().write(String.valueOf(x).getBytes(Charsets.ISO_8859_1));
        getStandardOutput().write(SPACE);
        getStandardOutput().write(String.valueOf(y).getBytes(Charsets.ISO_8859_1));
        getStandardOutput().writeEOL();
    }

    private void writeXrefEntry(COSWriterXRefEntry entry) throws IOException
    {
        String offset = formatXrefOffset.format(entry.getOffset());
        String generation = formatXrefGeneration.format(entry.getKey().getGeneration());
        getStandardOutput().write(offset.getBytes(Charsets.ISO_8859_1));
        getStandardOutput().write(SPACE);
        getStandardOutput().write(generation.getBytes(Charsets.ISO_8859_1));
        getStandardOutput().write(SPACE);
        getStandardOutput().write(entry.isFree() ? XREF_FREE : XREF_USED);
        getStandardOutput().writeCRLF();
    }

    /**
     * check the xref entries and write out the ranges.  The format of the
     * returned array is exactly the same as the pdf specification.  See section
     * 7.5.4 of ISO32000-1:2008, example 1 (page 40) for reference.
     * <p>
     * example: 0 1 2 5 6 7 8 10
     * <p>
     * will create a array with follow ranges
     * <p>
     * 0 3 5 4 10 1
     * <p>
     * this mean that the element 0 is followed by two other related numbers
     * that represent a cluster of the size 3. 5 is follow by three other
     * related numbers and create a cluster of size 4. etc.
     *
     * @param xRefEntriesList list with the xRef entries that was written
     * @return a integer array with the ranges
     */
    protected Long[] getXRefRanges(List<COSWriterXRefEntry> xRefEntriesList)
    {
        long last = -2;
        long count = 1;

        List<Long> list = new ArrayList<Long>();
        for( Object object : xRefEntriesList )
        {
            long nr = (int) ((COSWriterXRefEntry) object).getKey().getNumber();
            if (nr == last + 1)
            {
                ++count;
                last = nr;
            }
            else if (last == -2)
            {
                last = nr;
            }
            else
            {
                list.add(last - count + 1);
                list.add(count);
                last = nr;
                count = 1;
            }
        }
        // If no new entry is found, we need to write out the last result
        if(xRefEntriesList.size() > 0)
        {
            list.add(last - count + 1);
            list.add(count);
        }
        return list.toArray(new Long[list.size()]);
    }

    /**
     * This will get the object key for the object.
     *
     * @param obj The object to get the key for.
     *
     * @return The object key for the object.
     */
    private COSObjectKey getObjectKey( COSBase obj )
    {
        COSBase actual = obj;
        if( actual instanceof COSObject )
        {
            actual = ((COSObject)obj).getObject();
        }
        COSObjectKey key = null;
        if( actual != null )
        {
            key = objectKeys.get(actual);
        }
        if( key == null )
        {
            key = objectKeys.get(obj);
        }
        if (key == null)
        {
            setNumber(getNumber()+1);
            key = new COSObjectKey(getNumber(),0);
            objectKeys.put(obj, key);
            if( actual != null )
            {
                objectKeys.put(actual, key);
            }
        }
        return key;
    }

    @Override
    public Object visitFromArray( COSArray obj ) throws IOException
    {
        int count = 0;
        getStandardOutput().write(ARRAY_OPEN);
        for (Iterator<COSBase> i = obj.iterator(); i.hasNext();)
        {
            COSBase current = i.next();
            if( current instanceof COSDictionary )
            {
                if (current.isDirect())
                {
                    visitFromDictionary((COSDictionary)current);
                }
                else
                {
                    addObjectToWrite( current );
                    writeReference( current );
                }
            }
            else if( current instanceof COSObject )
            {
                COSBase subValue = ((COSObject)current).getObject();
                if( subValue instanceof COSDictionary || subValue == null )
                {
                    addObjectToWrite( current );
                    writeReference( current );
                }
                else
                {
                    subValue.accept( this );
                }
            }
            else if( current == null )
            {
                COSNull.NULL.accept( this );
            }
            else
            {
                current.accept(this);
            }
            count++;
            if (i.hasNext())
            {
                if (count % 10 == 0)
                {
                    getStandardOutput().writeEOL();
                }
                else
                {
                    getStandardOutput().write(SPACE);
                }
            }
        }
        getStandardOutput().write(ARRAY_CLOSE);
        getStandardOutput().writeEOL();
        return null;
    }

    @Override
    public Object visitFromBoolean(COSBoolean obj) throws IOException
    {
        obj.writePDF( getStandardOutput() );
        return null;
    }

    @Override
    public Object visitFromDictionary(COSDictionary obj) throws IOException
    {
        getStandardOutput().write(DICT_OPEN);
        getStandardOutput().writeEOL();
        for (Map.Entry<COSName, COSBase> entry : obj.entrySet())
        {
            COSBase value = entry.getValue();
            if (value != null)
            {
                entry.getKey().accept(this);
                getStandardOutput().write(SPACE);
                if( value instanceof COSDictionary )
                {
                    COSDictionary dict = (COSDictionary)value;

                    // write all XObjects as direct objects, this will save some size
                    COSBase item = dict.getItem(COSName.XOBJECT);
                    if(item!=null)
                    {
                        item.setDirect(true);
                    }
                    item = dict.getItem(COSName.RESOURCES);
                    if(item!=null)
                    {
                        item.setDirect(true);
                    }

                    if(dict.isDirect())
                    {
                        // If the object should be written direct, we need
                        // to pass the dictionary to the visitor again.
                        visitFromDictionary(dict);
                    }
                    else
                    {
                        addObjectToWrite( dict );
                        writeReference( dict );
                    }
                }
                else if( value instanceof COSObject )
                {
                    COSBase subValue = ((COSObject)value).getObject();
                    if( subValue instanceof COSDictionary || subValue == null )
                    {
                        addObjectToWrite( value );
                        writeReference( value );
                    }
                    else
                    {
                        subValue.accept( this );
                    }
                }
                else
                {
                    // If we reach the pdf signature, we need to determinate the position of the
                    // content and byterange
                    if(reachedSignature && COSName.CONTENTS.equals(entry.getKey()))
                    {
                        signatureOffset = getStandardOutput().getPos();
                        value.accept(this);
                        signatureLength = getStandardOutput().getPos()- signatureOffset;
                    }
                    else if(reachedSignature && COSName.BYTERANGE.equals(entry.getKey()))
                    {
                        byteRangeOffset = getStandardOutput().getPos() + 1;
                        value.accept(this);
                        byteRangeLength = getStandardOutput().getPos() - 1 - byteRangeOffset;
                        reachedSignature = false;
                    }
                    else
                    {
                        value.accept(this);
                    }
                }
                getStandardOutput().writeEOL();

            }
            else
            {
                //then we won't write anything, there are a couple cases
                //were the value of an entry in the COSDictionary will
                //be a dangling reference that points to nothing
                //so we will just not write out the entry if that is the case
            }
        }
        getStandardOutput().write(DICT_CLOSE);
        getStandardOutput().writeEOL();
        return null;
    }

    @Override
    public Object visitFromDocument(COSDocument doc) throws IOException
    {
        if(!incrementalUpdate)
        {
            doWriteHeader(doc);
        }
        else
        {
            // Sometimes the original file will be missing a newline at the end
            // In order to avoid having %%EOF the first object on the same line
            // as the %%EOF, we put a newline here. If there's already one at
            // the end of the file, an extra one won't hurt. PDFBOX-1051
            getStandardOutput().writeCRLF();
        }

        doWriteBody(doc);

        // get the previous trailer
        COSDictionary trailer = doc.getTrailer();
        long hybridPrev = -1;

        if (trailer != null)
        {
            hybridPrev = trailer.getLong(COSName.XREF_STM);
        }

        if(incrementalUpdate || doc.isXRefStream())
        {
            doWriteXRefInc(doc, hybridPrev);
        }
        else
        {
            doWriteXRefTable();
            doWriteTrailer(doc);
        }

        // write endof
        getStandardOutput().write(STARTXREF);
        getStandardOutput().writeEOL();
        getStandardOutput().write(String.valueOf(getStartxref()).getBytes(Charsets.ISO_8859_1));
        getStandardOutput().writeEOL();
        getStandardOutput().write(EOF);
        getStandardOutput().writeEOL();

        if(incrementalUpdate)
        {
            doWriteSignature();
        }

        return null;
    }

    @Override
    public Object visitFromFloat(COSFloat obj) throws IOException
    {
        obj.writePDF( getStandardOutput() );
        return null;
    }

    @Override
    public Object visitFromInt(COSInteger obj) throws IOException
    {
        obj.writePDF( getStandardOutput() );
        return null;
    }

    @Override
    public Object visitFromName(COSName obj) throws IOException
    {
        obj.writePDF( getStandardOutput() );
        return null;
    }

    @Override
    public Object visitFromNull(COSNull obj) throws IOException
    {
        obj.writePDF(getStandardOutput());
        return null;
    }

    /**
     * visitFromObjRef method comment.
     *
     * @param obj The object that is being visited.
     *
     * @throws IOException If there is an exception while visiting this object.
     */
    public void writeReference(COSBase obj) throws IOException
    {
        COSObjectKey key = getObjectKey(obj);
        getStandardOutput().write(String.valueOf(key.getNumber()).getBytes(Charsets.ISO_8859_1));
        getStandardOutput().write(SPACE);
        getStandardOutput().write(String.valueOf(key.getGeneration()).getBytes(Charsets.ISO_8859_1));
        getStandardOutput().write(SPACE);
        getStandardOutput().write(REFERENCE);
    }

    @Override
    public Object visitFromStream(COSStream obj) throws IOException
    {
        if (willEncrypt)
        {
            pdDocument.getEncryption().getSecurityHandler().encryptStream(obj,
                currentObjectKey.getNumber(), currentObjectKey.getGeneration());
        }

        InputStream input = null;
        try
        {
            // write the stream content
            visitFromDictionary(obj);
            getStandardOutput().write(STREAM);
            getStandardOutput().writeCRLF();

            input = obj.createRawInputStream();
            IOUtils.copy(input, getStandardOutput());

            getStandardOutput().writeCRLF();
            getStandardOutput().write(ENDSTREAM);
            getStandardOutput().writeEOL();
            return null;
        }
        finally
        {
            if (input != null)
            {
                input.close();
            }
        }
    }

    @Override
    public Object visitFromString(COSString obj) throws IOException
    {
        if (willEncrypt)
        {
            pdDocument.getEncryption().getSecurityHandler().encryptString(
                obj,
                currentObjectKey.getNumber(),
                currentObjectKey.getGeneration());
        }

        COSWriter.writeString(obj, getStandardOutput());
        return null;
    }

    /**
     * This will write the pdf document.
     *
     * @throws IOException If an error occurs while generating the data.
     * @param doc The document to write.
     */
    public void write(COSDocument doc) throws IOException
    {
        PDDocument pdDoc = new PDDocument( doc );
        write( pdDoc );
    }

    /**
     * This will write the pdf document.
     *
     * @param doc The document to write.
     *
     * @throws IOException If an error occurs while generating the data.
     */
    public void write(PDDocument doc) throws IOException
    {
        write(doc, null);
    }

    /**
     * This will write the pdf document.
     *
     * @param doc The document to write.
     * @param signInterface class to be used for signing
     *
     * @throws IOException If an error occurs while generating the data.
     * @throws IllegalStateException If the document has an encryption dictionary but no protection
     * policy.
     */
    public void write(PDDocument doc, SignatureInterface signInterface) throws IOException
    {
        Long idTime = doc.getDocumentId() == null ? System.currentTimeMillis() :
            doc.getDocumentId();

        pdDocument = doc;
        signatureInterface = signInterface;
        if(incrementalUpdate)
        {
            prepareIncrement(doc);
        }

        // if the document says we should remove encryption, then we shouldn't encrypt
        if(doc.isAllSecurityToBeRemoved())
        {
            willEncrypt = false;
            // also need to get rid of the "Encrypt" in the trailer so readers
            // don't try to decrypt a document which is not encrypted
            COSDocument cosDoc = doc.getDocument();
            COSDictionary trailer = cosDoc.getTrailer();
            trailer.removeItem(COSName.ENCRYPT);
        }
        else
        {
            if (pdDocument.getEncryption() != null)
            {
                SecurityHandler securityHandler = pdDocument.getEncryption().getSecurityHandler();
                if (!securityHandler.hasProtectionPolicy())
                {
                    throw new IllegalStateException("PDF contains an encryption dictionary, please remove it with "
                        + "setAllSecurityToBeRemoved() or set a protection policy with protect()");
                }
                securityHandler.prepareDocumentForEncryption(pdDocument);
                willEncrypt = true;
            }
            else
            {
                willEncrypt = false;
            }
        }

        COSDocument cosDoc = pdDocument.getDocument();
        COSDictionary trailer = cosDoc.getTrailer();
        COSArray idArray = (COSArray)trailer.getDictionaryObject( COSName.ID );
        boolean missingID = true;
        // check for an existing documentID
        if (idArray != null && idArray.size() == 2)
        {
            missingID = false;
        }
        if( missingID || incrementalUpdate)
        {
            MessageDigest md5;
            try
            {
                md5 = MessageDigest.getInstance("MD5");
            }
            catch (NoSuchAlgorithmException e)
            {
                // should never happen
                throw new RuntimeException(e);
            }

            // algorithm says to use time/path/size/values in doc to generate the id.
            // we don't have path or size, so do the best we can
            md5.update( Long.toString(idTime).getBytes(Charsets.ISO_8859_1) );

            COSDictionary info = (COSDictionary)trailer.getDictionaryObject( COSName.INFO );
            if( info != null )
            {
                Iterator<COSBase> values = info.getValues().iterator();
                while( values.hasNext() )
                {
                    md5.update( values.next().toString().getBytes(Charsets.ISO_8859_1) );
                }
            }
            // reuse origin documentID if available as first value
            COSString firstID = missingID ? new COSString( md5.digest() ) : (COSString)idArray.get(0);
            // it's ok to use the same ID for the second part if the ID is created for the first time
            COSString secondID = missingID ? firstID : new COSString( md5.digest() );
            idArray = new COSArray();
            idArray.add( firstID );
            idArray.add( secondID );
            trailer.setItem( COSName.ID, idArray );
        }
        cosDoc.accept(this);
    }

    /**
     * This will write the fdf document.
     *
     * @param doc The document to write.
     *
     * @throws IOException If an error occurs while generating the data.
     */
    public void write(FDFDocument doc) throws IOException
    {
        fdfDocument = doc;
        willEncrypt = false;
        COSDocument cosDoc = fdfDocument.getDocument();
        cosDoc.accept(this);
    }

    /**
     * This will output the given byte getString as a PDF object.
     *
     * @param string COSString to be written
     * @param output The stream to write to.
     * @throws IOException If there is an error writing to the stream.
     */
    public static void writeString(COSString string, OutputStream output) throws IOException
    {
        writeString(string.getBytes(), string.getForceHexForm(), output);
    }

    /**
     * This will output the given text/byte getString as a PDF object.
     *
     * @param bytes byte array representation of a string to be written
     * @param output The stream to write to.
     * @throws IOException If there is an error writing to the stream.
     */
    public static void writeString(byte[] bytes, OutputStream output) throws IOException
    {
        writeString(bytes, false, output);
    }

    /**
     * This will output the given text/byte string as a PDF object.
     *
     * @param output The stream to write to.
     * @throws IOException If there is an error writing to the stream.
     */
    private static void writeString(byte[] bytes, boolean forceHex, OutputStream output)
        throws IOException
    {
        // check for non-ASCII characters
        boolean isASCII = true;
        for (byte b : bytes)
        {
            // if the byte is negative then it is an eight bit byte and is outside the ASCII range
            if (b < 0)
            {
                isASCII = false;
                break;
            }
        }

        if (isASCII && !forceHex)
        {
            // write ASCII string
            output.write('(');
            for (byte b : bytes)
            {
                switch (b)
                {
                    case '(':
                    case ')':
                    case '\\':
                        output.write('\\');
                        output.write(b);
                        break;
                    default:
                        output.write(b);
                }
            }
            output.write(')');
        }
        else
        {
            // write hex string
            output.write('<');
            for (byte b : bytes)
            {
                output.write(Hex.getBytes(b));
            }
            output.write('>');
        }
    }
}
