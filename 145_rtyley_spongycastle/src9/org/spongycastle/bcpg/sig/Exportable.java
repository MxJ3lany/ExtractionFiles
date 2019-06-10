package org.spongycastle.bcpg.sig;

import org.spongycastle.bcpg.SignatureSubpacket;
import org.spongycastle.bcpg.SignatureSubpacketTags;

/**
 * packet giving signature creation time.
 */
public class Exportable 
    extends SignatureSubpacket
{    
    private static byte[] booleanToByteArray(
        boolean    value)
    {
        byte[]    data = new byte[1];
        
        if (value)
        {
            data[0] = 1;
            return data;
        }
        else
        {
            return data;
        }
    }
    
    public Exportable(
        boolean    critical,
        boolean    isLongLength,
        byte[]     data)
    {
        super(SignatureSubpacketTags.EXPORTABLE, critical, isLongLength, data);
    }
    
    public Exportable(
        boolean    critical,
        boolean    isExportable)
    {
        super(SignatureSubpacketTags.EXPORTABLE, critical, false,  booleanToByteArray(isExportable));
    }
    
    public boolean isExportable()
    {
        return data[0] != 0;
    }
}
