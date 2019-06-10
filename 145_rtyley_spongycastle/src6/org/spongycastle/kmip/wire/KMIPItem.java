package org.spongycastle.kmip.wire;

public interface KMIPItem<T>
    extends KMIPEncodable
{
    int getTag();

    byte getType();

    long getLength();

    T getValue();
}
