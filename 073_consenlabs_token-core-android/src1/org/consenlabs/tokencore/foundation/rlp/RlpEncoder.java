package org.consenlabs.tokencore.foundation.rlp;

import org.consenlabs.tokencore.foundation.utils.ByteUtil;

import java.util.Arrays;
import java.util.List;

/**
 * <p>Recursive Length Prefix (RLP) encoder.</p>
 *
 * <p>For the specification, refer to p16 of the <a href="http://gavwood.com/paper.pdf">
 * yellow paper</a> and <a href="https://github.com/ethereum/wiki/wiki/RLP">here</a>.</p>
 */
public class RlpEncoder {

    private static final int STRING_OFFSET = 0x80;
    private static final int LIST_OFFSET = 0xc0;

    public static byte[] encode(RlpType value) {
        if (value instanceof RlpString) {
            return encodeString((RlpString) value);
        } else {
            return encodeList((RlpList) value);
        }
    }

    private static byte[] encode(byte[] bytesValue, int offset) {
        if (bytesValue.length == 1
                && offset == STRING_OFFSET
                && bytesValue[0] >= (byte) 0x00
                && bytesValue[0] <= (byte) 0x7f) {
            return bytesValue;
        } else if (bytesValue.length <= 55) {
            byte[] result = new byte[bytesValue.length + 1];
            result[0] = (byte) (offset + bytesValue.length);
            System.arraycopy(bytesValue, 0, result, 1, bytesValue.length);
            return result;
        } else {
            byte[] encodedStringLength = toMinimalByteArray(bytesValue.length);
            byte[] result = new byte[bytesValue.length + encodedStringLength.length + 1];

            result[0] = (byte) ((offset + 0x37) + encodedStringLength.length);
            System.arraycopy(encodedStringLength, 0, result, 1, encodedStringLength.length);
            System.arraycopy(
                    bytesValue, 0, result, encodedStringLength.length + 1, bytesValue.length);
            return result;
        }
    }

    private static byte[] encodeString(RlpString value) {
        return encode(value.getBytes(), STRING_OFFSET);
    }

    private static byte[] toMinimalByteArray(int value) {
        byte[] encoded = toByteArray(value);

        for (int i = 0; i < encoded.length; i++) {
            if (encoded[i] != 0) {
                return Arrays.copyOfRange(encoded, i, encoded.length);
            }
        }

        return new byte[]{ };
    }

    private static byte[] toByteArray(int value) {
        return new byte[] {
                (byte) ((value >> 24) & 0xff),
                (byte) ((value >> 16) & 0xff),
                (byte) ((value >> 8) & 0xff),
                (byte) (value & 0xff)
        };
    }

    private static byte[] encodeList(RlpList value) {
        List<RlpType> values = value.getValues();
        if (values.isEmpty()) {
            return encode(new byte[]{ }, LIST_OFFSET);
        } else {
            byte[] result = new byte[0];
            for (RlpType entry:values) {
                result = ByteUtil.concat(result, encode(entry));
            }
            return encode(result, LIST_OFFSET);
        }
    }
}
