package org.spongycastle.jsse.provider;

import java.security.NoSuchAlgorithmException;

interface EngineCreator
{
    Object createInstance(Object constructorParameter)
        throws NoSuchAlgorithmException;
}
