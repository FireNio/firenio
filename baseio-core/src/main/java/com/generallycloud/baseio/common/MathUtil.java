/*
 * Copyright 2015-2017 GenerallyCloud.com
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
package com.generallycloud.baseio.common;

public class MathUtil {

    public static byte binaryString2byte(String string) {

        Assert.notNull(string, "null binary string");
        char c0 = '0';
        char c1 = '1';
        if (string.length() != 8) {
            throw new IllegalArgumentException("except length 8");
        }
        char[] cs = string.toCharArray();
        byte result = 0;
        for (int i = 0; i < 8; i++) {
            char c = cs[i];
            int x = 0;
            if (c0 == c) {} else if (c1 == c) {
                x = 1;
            } else {
                throw new IllegalArgumentException(String.valueOf(c));
            }
            result = (byte) ((x << (7 - i)) | result);
        }
        return result;
    }

    public static String binaryString2HexString(String string) {
        byte b = binaryString2byte(string);
        return byte2HexString(b);
    }

    public static String byte2BinaryString(byte b) {
        StringBuilder builder = new StringBuilder();
        for (int i = 7; i > -1; i--) {
            builder.append(getBoolean(b, i) ? '1' : '0');
        }
        return builder.toString();
    }

    public static String byte2HexString(byte b) {
        return HEXS[b & 0xFF];
    }

    public static int byte2Int(byte[] bytes, int offset) {

        checkLength(bytes, 4, offset);

        int v3 = (bytes[offset + 0] & 0xff) << 8 * 3;
        int v2 = (bytes[offset + 1] & 0xff) << 8 * 2;
        int v1 = (bytes[offset + 2] & 0xff) << 8 * 1;
        int v0 = (bytes[offset + 3] & 0xff);
        return v0 | v1 | v2 | v3;

    }

    public static int byte2Int31(byte[] bytes, int offset) {

        checkLength(bytes, 4, offset);

        int v3 = (bytes[offset + 0] & 0x7f) << 8 * 3;
        int v2 = (bytes[offset + 1] & 0xff) << 8 * 2;
        int v1 = (bytes[offset + 2] & 0xff) << 8 * 1;
        int v0 = (bytes[offset + 3] & 0xff);
        return v0 | v1 | v2 | v3;
    }

    public static int byte2Int31LE(byte[] bytes, int offset) {

        checkLength(bytes, 4, offset);

        int v0 = (bytes[offset + 0] & 0xff);
        int v1 = (bytes[offset + 1] & 0xff) << 8 * 1;
        int v2 = (bytes[offset + 2] & 0xff) << 8 * 2;
        int v3 = (bytes[offset + 3] & 0x7f) << 8 * 3;
        return v0 | v1 | v2 | v3;

    }

    public static int byte2IntLE(byte[] bytes, int offset) {

        checkLength(bytes, 4, offset);

        int v0 = (bytes[offset + 0] & 0xff);
        int v1 = (bytes[offset + 1] & 0xff) << 8 * 1;
        int v2 = (bytes[offset + 2] & 0xff) << 8 * 2;
        int v3 = (bytes[offset + 3] & 0xff) << 8 * 3;
        return v0 | v1 | v2 | v3;
    }

    public static long byte2Long(byte[] bytes, int offset) {

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

    public static long byte2LongLE(byte[] bytes, int offset) {

        checkLength(bytes, 8, offset);

        long v0 = bytes[offset + 0] & 0xff;
        long v1 = (long) (bytes[offset + 1] & 0xff) << 8 * 1;
        long v2 = (long) (bytes[offset + 2] & 0xff) << 8 * 2;
        long v3 = (long) (bytes[offset + 3] & 0xff) << 8 * 3;
        long v4 = (long) (bytes[offset + 4] & 0xff) << 8 * 4;
        long v5 = (long) (bytes[offset + 5] & 0xff) << 8 * 5;
        long v6 = (long) (bytes[offset + 6] & 0xff) << 8 * 6;
        long v7 = (long) (bytes[offset + 7] & 0xff) << 8 * 7;
        return (v0 | v1 | v2 | v3 | v4 | v5 | v6 | v7);
    }

    public static short byte2Short(byte[] bytes, int offset) {

        checkLength(bytes, 2, offset);

        int v1 = ((bytes[offset + 0] & 0xff) << 8 * 1);
        int v0 = (bytes[offset + 1] & 0xff);
        return (short) (v0 | v1);
    }

    public static short byte2ShortLE(byte[] bytes, int offset) {

        checkLength(bytes, 2, offset);

        int v0 = (bytes[offset + 0] & 0xff);
        int v1 = ((bytes[offset + 1] & 0xff) << 8 * 1);
        return (short) (v0 | v1);
    }

    public static long byte2UnsignedInt(byte[] bytes, int offset) {

        checkLength(bytes, 4, offset);

        long v3 = (long) (bytes[offset + 0] & 0xff) << 8 * 3;
        int v2 = (bytes[offset + 1] & 0xff) << 8 * 2;
        int v1 = (bytes[offset + 2] & 0xff) << 8 * 1;
        int v0 = (bytes[offset + 3] & 0xff);
        return v0 | v1 | v2 | v3;

    }

    public static long byte2UnsignedIntLE(byte[] bytes, int offset) {

        checkLength(bytes, 4, offset);

        int v0 = (bytes[offset + 0] & 0xff);
        int v1 = (bytes[offset + 1] & 0xff) << 8 * 1;
        int v2 = (bytes[offset + 2] & 0xff) << 8 * 2;
        long v3 = (long) (bytes[offset + 3] & 0xff) << 8 * 3;
        return v0 | v1 | v2 | v3;

    }

    public static int byte2UnsignedShort(byte[] bytes, int offset) {

        checkLength(bytes, 2, offset);

        int v1 = (bytes[offset + 0] & 0xff) << 8 * 1;
        int v0 = (bytes[offset + 1] & 0xff);
        return v0 | v1;

    }

    public static int byte2UnsignedShortLE(byte[] bytes, int offset) {

        checkLength(bytes, 2, offset);

        int v0 = (bytes[offset + 0] & 0xff);
        int v1 = (bytes[offset + 1] & 0xff) << 8 * 1;
        return v0 | v1;

    }

    public static String int2HexString(int value) {
        byte[] array = new byte[4];
        int2Byte(array, value, 0);
        return bytes2HexString(array);
    }

    public static String long2HexString(long value) {
        byte[] array = new byte[8];
        long2Byte(array, value, 0);
        return bytes2HexString(array);
    }

    public static String bytes2HexString(byte[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        StringBuilder builder = new StringBuilder(array.length * 2);
        for (int i = 0; i < array.length; i++) {
            builder.append(byte2HexString(array[i]));
        }
        return builder.toString();
    }

    public static String bytes2HexString0X(byte[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        StringBuilder builder = new StringBuilder(array.length * 5 + 1);
        builder.append("[");
        for (int i = 0; i < array.length; i++) {
            builder.append("0x");
            builder.append(byte2HexString(array[i]));
            builder.append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("]");
        return builder.toString();
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
        if (pos < 0 || pos > 8) {
            throw new IllegalArgumentException("illegal pos," + pos);
        }
        return (b & (1 << pos)) >> pos == 1;
    }

    public static void int2Byte(byte[] bytes, int value, int offset) {

        checkLength(bytes, 4, offset);

        bytes[offset + 0] = (byte) (value >> 8 * 3);
        bytes[offset + 1] = (byte) (value >> 8 * 2);
        bytes[offset + 2] = (byte) (value >> 8 * 1);
        bytes[offset + 3] = (byte) (value);
    }

    public static void int2ByteLE(byte[] bytes, int value, int offset) {

        checkLength(bytes, 4, offset);

        bytes[offset + 0] = (byte) (value);
        bytes[offset + 1] = (byte) (value >> 8 * 1);
        bytes[offset + 2] = (byte) (value >> 8 * 2);
        bytes[offset + 3] = (byte) (value >> 8 * 3);
    }

    public static void unsignedInt2Byte(byte[] bytes, long value, int offset) {

        checkLength(bytes, 4, offset);

        bytes[offset + 0] = (byte) (value >> 8 * 3);
        bytes[offset + 1] = (byte) (value >> 8 * 2);
        bytes[offset + 2] = (byte) (value >> 8 * 1);
        bytes[offset + 3] = (byte) (value);
    }

    public static void unsignedInt2ByteLE(byte[] bytes, long value, int offset) {

        checkLength(bytes, 4, offset);

        bytes[offset + 0] = (byte) (value);
        bytes[offset + 1] = (byte) (value >> 8 * 1);
        bytes[offset + 2] = (byte) (value >> 8 * 2);
        bytes[offset + 3] = (byte) (value >> 8 * 3);
    }

    /*
     * -------------------------------------------------------------------------
     * ------
     */

    public static boolean isOutOfBounds(int index, int length, int capacity) {
        return (index | length | (index + length) | (capacity - (index + length))) < 0;
    }

    public static void long2Byte(byte[] bytes, long value, int offset) {

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

    public static void long2ByteLE(byte[] bytes, long value, int offset) {

        checkLength(bytes, 8, offset);

        bytes[offset + 0] = (byte) (value);
        bytes[offset + 1] = (byte) (value >> 8 * 1);
        bytes[offset + 2] = (byte) (value >> 8 * 2);
        bytes[offset + 3] = (byte) (value >> 8 * 3);
        bytes[offset + 4] = (byte) (value >> 8 * 4);
        bytes[offset + 5] = (byte) (value >> 8 * 5);
        bytes[offset + 6] = (byte) (value >> 8 * 6);
        bytes[offset + 7] = (byte) (value >> 8 * 7);

    }

    /*----------------------------------------------------------------------------*/

    public static int safeFindNextPositivePowerOfTwo(final int value) {
        return value <= 0 ? 1
                : value >= 0x40000000 ? 0x40000000 : findNextPositivePowerOfTwo(value);
    }

    public static void short2Byte(byte[] bytes, short value, int offset) {

        checkLength(bytes, 2, offset);

        bytes[offset + 0] = (byte) (value >> 8 * 1);
        bytes[offset + 1] = (byte) (value);
    }

    public static void short2ByteLE(byte[] bytes, short value, int offset) {

        checkLength(bytes, 2, offset);

        bytes[offset + 0] = (byte) (value);
        bytes[offset + 1] = (byte) (value >> 8 * 1);
    }

    public static void unsignedShort2Byte(byte[] bytes, int value, int offset) {

        checkLength(bytes, 2, offset);

        bytes[offset + 0] = (byte) (value >> 8 * 1);
        bytes[offset + 1] = (byte) (value);
    }

    public static void unsignedShort2ByteLE(byte[] bytes, int value, int offset) {

        checkLength(bytes, 2, offset);

        bytes[offset + 0] = (byte) (value);
        bytes[offset + 1] = (byte) (value >> 8 * 1);
    }

    public static int int2int31(int value) {
        if (value < 0) {
            return value & 0x7FFFFFFF;
        }
        return value;
    }

    public static void main(String[] args) {

        int v = Integer.MIN_VALUE + 2147299999;
        byte[] bytes = new byte[4];
        int2Byte(bytes, v, 0);

        System.out.println(bytes2HexString0X(bytes));
        System.out.println(bytes2HexString(bytes));
        int v1 = byte2Int(bytes, 0);

        System.out.println(v1);
        System.out.println(v1 == v);
    }

    private static String[] HEXS = new String[] { "00", "01", "02", "03", "04", "05", "06", "07",
            "08", "09", "0a", "0b", "0c", "0d", "0e", "0f", "10", "11", "12", "13", "14", "15",
            "16", "17", "18", "19", "1a", "1b", "1c", "1d", "1e", "1f", "20", "21", "22", "23",
            "24", "25", "26", "27", "28", "29", "2a", "2b", "2c", "2d", "2e", "2f", "30", "31",
            "32", "33", "34", "35", "36", "37", "38", "39", "3a", "3b", "3c", "3d", "3e", "3f",
            "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "4a", "4b", "4c", "4d",
            "4e", "4f", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "5a", "5b",
            "5c", "5d", "5e", "5f", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69",
            "6a", "6b", "6c", "6d", "6e", "6f", "70", "71", "72", "73", "74", "75", "76", "77",
            "78", "79", "7a", "7b", "7c", "7d", "7e", "7f", "80", "81", "82", "83", "84", "85",
            "86", "87", "88", "89", "8a", "8b", "8c", "8d", "8e", "8f", "90", "91", "92", "93",
            "94", "95", "96", "97", "98", "99", "9a", "9b", "9c", "9d", "9e", "9f", "a0", "a1",
            "a2", "a3", "a4", "a5", "a6", "a7", "a8", "a9", "aa", "ab", "ac", "ad", "ae", "af",
            "b0", "b1", "b2", "b3", "b4", "b5", "b6", "b7", "b8", "b9", "ba", "bb", "bc", "bd",
            "be", "bf", "c0", "c1", "c2", "c3", "c4", "c5", "c6", "c7", "c8", "c9", "ca", "cb",
            "cc", "cd", "ce", "cf", "d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9",
            "da", "db", "dc", "dd", "de", "df", "e0", "e1", "e2", "e3", "e4", "e5", "e6", "e7",
            "e8", "e9", "ea", "eb", "ec", "ed", "ee", "ef", "f0", "f1", "f2", "f3", "f4", "f5",
            "f6", "f7", "f8", "f9", "fa", "fb", "fc", "fd", "fe", "ff" };

}
