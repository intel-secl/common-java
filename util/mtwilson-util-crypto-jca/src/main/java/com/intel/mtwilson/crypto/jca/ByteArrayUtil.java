/*
 * Copyright 2016 Intel Corporation. All rights reserved.
 */
package com.intel.mtwilson.crypto.jca;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 *
 * @author jbuhacoff
 */
public class ByteArrayUtil {

    private static final Charset UTF8 = Charset.forName("UTF-8"); // java runtime guarantees availability of utf-8 so UnsupportedEncodingException will never happen here

    public static byte[] toByteArray(String text) {
        return ((String) text).getBytes(UTF8);
    }

    public static byte[] toByteArray(int number) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.SIZE / 8); // 4 bytes
        buffer.putInt(number);
        return buffer.array();
    }

    public static byte[] toByteArray(char[] text) {
        ByteBuffer encoded = UTF8.encode(CharBuffer.wrap(text));
        return Arrays.copyOf(encoded.array(), encoded.limit());
    }

    public static byte[] concat(byte[]... arrays) {
        int resultsize = 0;
        for (byte[] array : arrays) {
            resultsize += array.length;
        }
        byte[] result = new byte[resultsize];
        int cursor = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, cursor, array.length);
            cursor += array.length;
        }
        return result;
    }

}
