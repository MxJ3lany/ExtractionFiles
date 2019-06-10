
// Copyright 2014 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

// This file is autogenerated by:
//     mojo/public/tools/bindings/mojom_bindings_generator.py
// For:
//     ui/gfx/geometry/mojo/geometry.mojom
//

package org.chromium.gfx.mojom;

import org.chromium.base.annotations.SuppressFBWarnings;
import org.chromium.mojo.bindings.DeserializationException;


public final class RectF extends org.chromium.mojo.bindings.Struct {

    private static final int STRUCT_SIZE = 24;
    private static final org.chromium.mojo.bindings.DataHeader[] VERSION_ARRAY = new org.chromium.mojo.bindings.DataHeader[] {new org.chromium.mojo.bindings.DataHeader(24, 0)};
    private static final org.chromium.mojo.bindings.DataHeader DEFAULT_STRUCT_INFO = VERSION_ARRAY[0];
    public float x;
    public float y;
    public float width;
    public float height;

    private RectF(int version) {
        super(STRUCT_SIZE, version);
    }

    public RectF() {
        this(0);
    }

    public static RectF deserialize(org.chromium.mojo.bindings.Message message) {
        return decode(new org.chromium.mojo.bindings.Decoder(message));
    }

    /**
     * Similar to the method above, but deserializes from a |ByteBuffer| instance.
     *
     * @throws org.chromium.mojo.bindings.DeserializationException on deserialization failure.
     */
    public static RectF deserialize(java.nio.ByteBuffer data) {
        if (data == null)
            return null;

        return deserialize(new org.chromium.mojo.bindings.Message(
                data, new java.util.ArrayList<org.chromium.mojo.system.Handle>()));
    }

    @SuppressWarnings("unchecked")
    public static RectF decode(org.chromium.mojo.bindings.Decoder decoder0) {
        if (decoder0 == null) {
            return null;
        }
        decoder0.increaseStackDepth();
        RectF result;
        try {
            org.chromium.mojo.bindings.DataHeader mainDataHeader = decoder0.readAndValidateDataHeader(VERSION_ARRAY);
            result = new RectF(mainDataHeader.elementsOrVersion);
            if (mainDataHeader.elementsOrVersion >= 0) {
                
                result.x = decoder0.readFloat(8);
            }
            if (mainDataHeader.elementsOrVersion >= 0) {
                
                result.y = decoder0.readFloat(12);
            }
            if (mainDataHeader.elementsOrVersion >= 0) {
                
                result.width = decoder0.readFloat(16);
            }
            if (mainDataHeader.elementsOrVersion >= 0) {
                
                result.height = decoder0.readFloat(20);
            }
        } finally {
            decoder0.decreaseStackDepth();
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected final void encode(org.chromium.mojo.bindings.Encoder encoder) {
        org.chromium.mojo.bindings.Encoder encoder0 = encoder.getEncoderAtDataOffset(DEFAULT_STRUCT_INFO);
        
        encoder0.encode(x, 8);
        
        encoder0.encode(y, 12);
        
        encoder0.encode(width, 16);
        
        encoder0.encode(height, 20);
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this)
            return true;
        if (object == null)
            return false;
        if (getClass() != object.getClass())
            return false;
        RectF other = (RectF) object;
        if (this.x!= other.x)
            return false;
        if (this.y!= other.y)
            return false;
        if (this.width!= other.width)
            return false;
        if (this.height!= other.height)
            return false;
        return true;
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = prime + getClass().hashCode();
        result = prime * result + org.chromium.mojo.bindings.BindingsHelper.hashCode(x);
        result = prime * result + org.chromium.mojo.bindings.BindingsHelper.hashCode(y);
        result = prime * result + org.chromium.mojo.bindings.BindingsHelper.hashCode(width);
        result = prime * result + org.chromium.mojo.bindings.BindingsHelper.hashCode(height);
        return result;
    }
}