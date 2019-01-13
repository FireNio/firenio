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
package com.firenio.baseio;

import java.util.TreeMap;

import com.firenio.baseio.common.ByteUtil;

/**
 * @author wangkai
 *
 */
public class AsciiString implements CharSequence, Comparable<AsciiString> {

    private int    hash;
    private int    length;
    private int    offset;
    private String string;
    private byte[] value;

    public AsciiString(byte[] value) {
        this(value, 0, value.length);
    }

    public AsciiString(byte[] value, int offset, int length) {
        this.value = value;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public char charAt(int index) {
        return (char) (value[offset + index] & 0xff);
    }

    @Override
    public int compareTo(AsciiString o) {
        int len1 = length;
        int len2 = o.length();
        int off = offset;
        int lim = Math.min(len1, len2);
        byte v1[] = value;
        byte v2[] = o.value;

        int k = 0;
        while (k < lim) {
            byte c1 = v1[k + off];
            byte c2 = v2[k];
            if (c1 != c2) {
                return c1 - c2;
            }
            k++;
        }
        return len1 - len2;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != AsciiString.class) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        AsciiString other = (AsciiString) obj;
        return length() == other.length()
                && ByteUtil.equalsArray(value, offset, other.value, other.offset, length);
    }

    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0 && length > 0) {
            byte val[] = value;
            for (int i = offset, end = offset + length; i < end; i++) {
                h = 31 * h + val[i];
            }
            hash = h;
        }
        return h;
    }

    @Override
    public int length() {
        return length;
    }

    public void reset(byte[] value, int offset, int length) {
        this.value = value;
        this.offset = offset;
        this.length = length;
        this.hash = 0;
        this.string = null;
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new AsciiString(value, start, end);
    }

    @Override
    @SuppressWarnings("deprecation")
    public String toString() {
        if (string == null) {
            string = new String(value, 0, offset, length);
        }
        return string;
    }

    public static void main(String[] args) {
        TreeMap<AsciiString, String> map = new TreeMap<>();
        put(map, "123");
        put(map, "122");
        put(map, "121");
        put(map, "221");
        put(map, "212");
        put(map, "222");
        put(map, "211");
        put(map, "/plaintext");
        put(map, "/json");
        String res = map.get(new AsciiString("abc/plaintext123".getBytes(), 3, 10));
        System.out.println(res);
    }

    static void put(TreeMap<AsciiString, String> map, String v) {
        map.put(new AsciiString(v.getBytes()), v);
    }

}
