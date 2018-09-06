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

import java.util.Random;

public class MathUtil {

    public static byte binaryString2byte(String string) {
        Assert.notNull(string, "null binary string");
        char c0 = '0';
        char c1 = '1';
        if (string.length() != 8) {
            throw new IllegalArgumentException("except length 8");
        }
        char[] cs = StringUtil.stringToCharArray(string);
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

    public static byte[] hexString2bytes(char[] data) {
        int len = data.length;
        if ((len & 0x01) != 0) {
            throw new RuntimeException("Odd number of characters.");
        }
        byte[] out = new byte[len >> 1];
        // two characters form the hex value.
        //49
        //58
        //98
        //103
        for (int i = 0; i < data.length;) {
            int t = i >> 1;
            int ch0 = hex2Digit(data[i], i++);
            int ch1 = hex2Digit(data[i], i++);
            out[t] = (byte) (ch0 << 4 | ch1);
        }
        return out;
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
        return getBit(b, pos) == 1;
    }
    
    public static byte getBit(byte b, int pos) {
        if (pos < 0 || pos > 8) {
            throw new IllegalArgumentException("illegal pos," + pos);
        }
        return (byte) ((b >> pos) & 1);
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

        int v = new Random().nextInt(Integer.MAX_VALUE);
        byte[] bytes = new byte[4];
        int2Byte(bytes, v, 0);

        String hex = bytes2HexString(bytes);
        bytes = hexString2bytes(StringUtil.stringToCharArray(hex));
        System.out.println(v);
        System.out.println(hex);
        int v1 = byte2Int(bytes, 0);

        System.out.println(v1);
        System.out.println(v1 == v);

        System.out.println('0' + 1);
        System.out.println('9' + 1);
        System.out.println('a' + 1);
        System.out.println('f' + 1);
        System.out.println('A' + 1);
        System.out.println('F' + 1);

    }

    private static String[] HEXS = new String[] { "00", "01", "02", "03", "04", "05", "06", "07",
            "08", "09", "0A", "0B", "0C", "0D", "0E", "0F", "10", "11", "12", "13", "14", "15",
            "16", "17", "18", "19", "1A", "1B", "1C", "1D", "1E", "1F", "20", "21", "22", "23",
            "24", "25", "26", "27", "28", "29", "2A", "2B", "2C", "2D", "2E", "2F", "30", "31",
            "32", "33", "34", "35", "36", "37", "38", "39", "3A", "3B", "3C", "3D", "3E", "3F",
            "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "4A", "4B", "4C", "4D",
            "4E", "4F", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "5A", "5B",
            "5C", "5D", "5E", "5F", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69",
            "6A", "6B", "6C", "6D", "6E", "6F", "70", "71", "72", "73", "74", "75", "76", "77",
            "78", "79", "7A", "7B", "7C", "7D", "7E", "7F", "80", "81", "82", "83", "84", "85",
            "86", "87", "88", "89", "8A", "8B", "8C", "8D", "8E", "8F", "90", "91", "92", "93",
            "94", "95", "96", "97", "98", "99", "9A", "9B", "9C", "9D", "9E", "9F", "A0", "A1",
            "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9", "AA", "AB", "AC", "AD", "AE", "AF",
            "B0", "B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", "B9", "BA", "BB", "BC", "BD",
            "BE", "BF", "C0", "C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9", "CA", "CB",
            "CC", "CD", "CE", "CF", "D0", "D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8", "D9",
            "DA", "DB", "DC", "DD", "DE", "DF", "E0", "E1", "E2", "E3", "E4", "E5", "E6", "E7",
            "E8", "E9", "EA", "EB", "EC", "ED", "EE", "EF", "F0", "F1", "F2", "F3", "F4", "F5",
            "F6", "F7", "F8", "F9", "FA", "FB", "FC", "FD", "FE", "FF" };

}
