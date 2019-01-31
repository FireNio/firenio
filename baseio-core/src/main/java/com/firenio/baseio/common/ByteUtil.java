/*
 * Copyright 2015 The Baseio Project
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.firenio.baseio.common;

import java.nio.ByteBuffer;
import java.util.Random;

import com.firenio.baseio.buffer.ByteBuf;

public class ByteUtil {

    private static final String[] HEXS      = new String[256];
    private static final String[] NUMS      = new String[256];
    private static final byte[]   CHAR_NUMS = new byte[10];

    static {
        for (int i = 0; i < 16; i++) {
            HEXS[i] = "0" + Integer.toHexString(i);
            NUMS[i] = String.valueOf(i);
        }
        for (int i = 16; i < HEXS.length; i++) {
            HEXS[i] = Integer.toHexString(i);
            NUMS[i] = String.valueOf(i);
        }
        for (int i = 0; i < CHAR_NUMS.length; i++) {
            CHAR_NUMS[i] = (byte) NUMS[i].charAt(0);
        }
    }

    private static void checkLength(byte[] bytes, int length, int offset) {
        //		if (bytes == null) {
        //			throw new IllegalArgumentException("null");
        //		}
        //
        //		if (offset < 0) {
        //			throw new IllegalArgumentException("invalidate offset " + offset);
        //		}
        //
        //		if (bytes.length - offset < length) {
        //			throw new IllegalArgumentException("invalidate length " + bytes.length);
        //		}
    }

    public static int compare(long x, long y) {
        return (x < y) ? -1 : (x > y) ? 1 : 0;
    }

    public static boolean equalsArray(byte[] bs1, byte[] bs2) {
        if (bs1 == null || bs2 == null) {
            return false;
        }
        if (bs1.length != bs2.length) {
            return false;
        }
        for (int i = 0; i < bs1.length; i++) {
            if (bs1[i] != bs2[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean equalsArray(byte[] bs1, int off1, byte[] bs2, int off2, int len) {
        for (int i = 0; i < len; i++) {
            if (bs1[off1 + i] != bs2[off2 + i]) {
                return false;
            }
        }
        return true;
    }

    public static int findNextPositivePowerOfTwo(final int value) {
        assert value > Integer.MIN_VALUE && value < 0x40000000;
        return 1 << (32 - Integer.numberOfLeadingZeros(value - 1));
    }

    /**
     * 右起0 ~ 7
     * 
     * @param b
     * @param pos
     * @return
     */
    public static boolean getBoolean(byte b, int pos) {
        return ((b >>> pos) & 1) != 0;
    }

    public static byte[] getBytesFromHexString(String data) {
        int len = data.length();
        if ((len & 0x01) != 0) {
            throw new RuntimeException("Odd number of characters.");
        }
        byte[] out = new byte[len >> 1];
        // two characters form the hex value.
        //49
        //58
        //98
        //103
        for (int i = 0; i < data.length();) {
            int t = i >> 1;
            int ch0 = hex2Digit(data.charAt(i), i++);
            int ch1 = hex2Digit(data.charAt(i), i++);
            out[t] = (byte) (ch0 << 4 | ch1);
        }
        return out;
    }

    public static String getHexString(byte b) {
        return HEXS[b & 0xFF];
    }

    public static String getHexString(byte[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        StringBuilder builder = new StringBuilder(array.length * 2);
        for (int i = 0; i < array.length; i++) {
            builder.append(getHexString(array[i]));
        }
        return builder.toString();
    }

    public static String getHexString(int value) {
        byte[] array = new byte[4];
        putInt(array, value, 0);
        return getHexString(array);
    }

    public static String getHexString(long value) {
        byte[] array = new byte[8];
        putLong(array, value, 0);
        return getHexString(array);
    }

    public static String getHexString0X(byte[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        StringBuilder builder = new StringBuilder(array.length * 5 + 1);
        builder.append("[");
        for (int i = 0; i < array.length; i++) {
            builder.append("0x");
            builder.append(getHexString(array[i]));
            builder.append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("]");
        return builder.toString();
    }

    public static int getInt(byte[] bytes, int offset) {
        checkLength(bytes, 4, offset);
        int v3 = (bytes[offset + 0] & 0xff) << 8 * 3;
        int v2 = (bytes[offset + 1] & 0xff) << 8 * 2;
        int v1 = (bytes[offset + 2] & 0xff) << 8 * 1;
        int v0 = (bytes[offset + 3] & 0xff);
        return v0 | v1 | v2 | v3;
    }

    public static int getInt(long address) {
        if (Unsafe.bigOrder()) {
            return Unsafe.getInt(address);
        } else {
            return Integer.reverseBytes(Unsafe.getInt(address));
        }
    }

    public static int getInt31(byte[] bytes, int offset) {
        return getInt(bytes, offset) & 0x7fffffff;
    }

    public static int getInt31(int value) {
        if (value < 0) {
            return value & 0x7FFFFFFF;
        }
        return value;
    }

    public static int getIntLE(byte[] bytes, int offset) {
        return Integer.reverseBytes(getInt(bytes, offset));
    }

    public static int getIntLE(long address) {
        if (Unsafe.littleOrder()) {
            return Unsafe.getShort(address);
        } else {
            return Integer.reverseBytes(Unsafe.getShort(address));
        }
    }

    public static long getLong(byte[] bytes, int offset) {
        checkLength(bytes, 8, offset);
        long v7 = (long) (bytes[offset + 0] & 0xff) << 8 * 7;
        long v6 = (long) (bytes[offset + 1] & 0xff) << 8 * 6;
        long v5 = (long) (bytes[offset + 2] & 0xff) << 8 * 5;
        long v4 = (long) (bytes[offset + 3] & 0xff) << 8 * 4;
        long v3 = (long) (bytes[offset + 4] & 0xff) << 8 * 3;
        long v2 = (long) (bytes[offset + 5] & 0xff) << 8 * 2;
        long v1 = (long) (bytes[offset + 6] & 0xff) << 8 * 1;
        long v0 = bytes[offset + 7] & 0xff;
        return (v0 | v1 | v2 | v3 | v4 | v5 | v6 | v7);
    }

    public static long getLong(long address) {
        if (Unsafe.bigOrder()) {
            return Unsafe.getLong(address);
        } else {
            return Long.reverseBytes(Unsafe.getLong(address));
        }
    }

    public static long getLongLE(byte[] bytes, int offset) {
        return Long.reverseBytes(getLong(bytes, offset));
    }

    public static long getLongLE(long address) {
        if (Unsafe.littleOrder()) {
            return Unsafe.getLong(address);
        } else {
            return Long.reverseBytes(Unsafe.getLong(address));
        }
    }

    public static String getNumString(byte b) {
        return NUMS[b & 0xFF];
    }

    public static byte getNumCharByte(int value) {
        return CHAR_NUMS[value];
    }

    public static short getShort(byte[] bytes, int offset) {
        checkLength(bytes, 2, offset);
        int v1 = ((bytes[offset + 0] & 0xff) << 8 * 1);
        int v0 = (bytes[offset + 1] & 0xff);
        return (short) (v0 | v1);
    }

    public static short getShort(long address) {
        if (Unsafe.bigOrder()) {
            return Unsafe.getShort(address);
        } else {
            return Short.reverseBytes(Unsafe.getShort(address));
        }
    }

    public static short getShortLE(byte[] bytes, int offset) {
        return Short.reverseBytes(getShort(bytes, offset));
    }

    public static short getShortLE(long address) {
        if (Unsafe.littleOrder()) {
            return Unsafe.getShort(address);
        } else {
            return Short.reverseBytes(Unsafe.getShort(address));
        }
    }

    private static int hex2Digit(char ch, int index) {
        if (ch < 48) {
            throw new RuntimeException("Illegal hexadecimal charcter " + ch + " at index " + index);
        } else if (ch < 58) {
            return ch - 48;
        } else if (ch < 65) {
            throw new RuntimeException("Illegal hexadecimal charcter " + ch + " at index " + index);
        } else if (ch < 71) {
            return ch - 55;
        } else if (ch < 97) {
            throw new RuntimeException("Illegal hexadecimal charcter " + ch + " at index " + index);
        } else if (ch < 103) {
            return ch - 87;
        } else {
            throw new RuntimeException("Illegal hexadecimal charcter " + ch + " at index " + index);
        }
    }

    public static boolean isOutOfBounds(int index, int length, int capacity) {
        return (index | length | (index + length) | (capacity - (index + length))) < 0;
    }

    public static void main(String[] args) {

        int v = new Random().nextInt(Integer.MAX_VALUE);
        byte[] bytes = new byte[4];
        putInt(bytes, v, 0);

        String hex = getHexString(bytes);
        bytes = getBytesFromHexString(hex);
        System.out.println(v);
        System.out.println(hex);
        int v1 = getInt(bytes, 0);

        System.out.println(v1);
        System.out.println(v1 == v);

        System.out.println('0' + 1);
        System.out.println('9' + 1);
        System.out.println('a' + 1);
        System.out.println('f' + 1);
        System.out.println('A' + 1);
        System.out.println('F' + 1);

    }

    public static void putInt(byte[] bytes, int value, int offset) {
        checkLength(bytes, 4, offset);
        bytes[offset + 0] = (byte) (value >> 8 * 3);
        bytes[offset + 1] = (byte) (value >> 8 * 2);
        bytes[offset + 2] = (byte) (value >> 8 * 1);
        bytes[offset + 3] = (byte) (value);
    }

    public static void putInt(long address, int value) {
        if (Unsafe.bigOrder()) {
            Unsafe.putInt(address, value);
        } else {
            Unsafe.putInt(address, Integer.reverseBytes(value));
        }
    }

    public static void putIntLE(byte[] bytes, int value, int offset) {
        putInt(bytes, Integer.reverseBytes(value), offset);
    }

    public static void putIntLE(long address, int value) {
        if (Unsafe.littleOrder()) {
            Unsafe.putInt(address, value);
        } else {
            Unsafe.putInt(address, Integer.reverseBytes(value));
        }
    }

    public static void putLong(byte[] bytes, long value, int offset) {
        checkLength(bytes, 8, offset);
        bytes[offset + 0] = (byte) (value >> 8 * 7);
        bytes[offset + 1] = (byte) (value >> 8 * 6);
        bytes[offset + 2] = (byte) (value >> 8 * 5);
        bytes[offset + 3] = (byte) (value >> 8 * 4);
        bytes[offset + 4] = (byte) (value >> 8 * 3);
        bytes[offset + 5] = (byte) (value >> 8 * 2);
        bytes[offset + 6] = (byte) (value >> 8 * 1);
        bytes[offset + 7] = (byte) (value);
    }

    public static void putLong(long address, long value) {
        if (Unsafe.bigOrder()) {
            Unsafe.putLong(address, value);
        } else {
            Unsafe.putLong(address, Long.reverseBytes(value));
        }
    }

    public static void putLongLE(byte[] bytes, long value, int offset) {
        putLong(bytes, Long.reverseBytes(value), offset);
    }

    public static void putLongLE(long address, long value) {
        if (Unsafe.littleOrder()) {
            Unsafe.putLong(address, value);
        } else {
            Unsafe.putLong(address, Long.reverseBytes(value));
        }
    }

    public static void putShort(byte[] bytes, short value, int offset) {
        checkLength(bytes, 2, offset);
        bytes[offset + 0] = (byte) (value >> 8 * 1);
        bytes[offset + 1] = (byte) (value);
    }

    public static void putShort(long address, short value) {
        if (Unsafe.bigOrder()) {
            Unsafe.putShort(address, value);
        } else {
            Unsafe.putShort(address, Short.reverseBytes(value));
        }
    }

    public static void putShortLE(byte[] bytes, short value, int offset) {
        putShort(bytes, Short.reverseBytes(value), offset);
    }

    public static void putShortLE(long address, short value) {
        if (Unsafe.littleOrder()) {
            Unsafe.putShort(address, value);
        } else {
            Unsafe.putShort(address, Short.reverseBytes(value));
        }
    }

    public static int safeFindNextPositivePowerOfTwo(final int value) {
        return value <= 0 ? 1
                : value >= 0x40000000 ? 0x40000000 : findNextPositivePowerOfTwo(value);
    }
    

    @SuppressWarnings("restriction")
    public static void free(ByteBuffer buffer) {
        if (((sun.nio.ch.DirectBuffer) buffer).cleaner() != null) {
            ((sun.nio.ch.DirectBuffer) buffer).cleaner().clean();
        }
    }

    public static int skip(ByteBuf src, int p, int e, byte v) {
        int i = p;
        for (; i < e; i++) {
            if (src.absByte(i) != v) {
                return i;
            }
        }
        return -1;
    }
    
    public static byte[] b(String s) {
        return s.getBytes();
    }


}
