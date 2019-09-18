/*
 * Copyright 2015 The FireNio Project
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
package com.firenio.codec.http11;

import com.firenio.common.Util;

//关键字：前缀，后缀，部分匹配表
public class KMPUtil {

    private char[] match_array;

    private int[] match_table;

    private String match_value;

    public KMPUtil(String value) {
        if (Util.isNullOrBlank(value)) {
            throw new IllegalArgumentException("null value");
        }
        this.match_value = value;
        this.match_array = match_value.toCharArray();
        this.match_table = new int[match_value.length() - 1];
        int length = this.match_value.length() - 1;
        // 只需要计算长度减一位的部分匹配表，因为最后一位如果也匹配整个字符串就匹配了
        // 直接从第二位开始比较
        for (int i = 1; i < length; i++) {
            match_table[i] = init_table(value, i + 1);
        }
    }

    public static void main(String[] args) {

        String  src   = "ABC ABCA ABCABA";
        String  match = "ABCAB";
        src = "AABAABAABC";
        match = "AABAABC";
        KMPUtil kmp   = new KMPUtil(match);
        int     find  = 0;
        for (; ; ) {
            find = kmp.match(src, find);
            if (find == -1) {
                break;
            }
            System.out.println(find);
            find += match.length();
        }

    }

    private int init_table(String value, int length) {
        int e = 0;
        WORD:
        for (int i = 1; i < length; i++) {
            for (int j = 0; j < i; j++) {
                int fix = length - i;
                if (value.charAt(j) != value.charAt(fix + j)) {
                    continue WORD;
                }
            }
            e = i;
        }
        return e;
    }

    public int match(String value) {
        return match(value, 0);
    }

    public int match(String value, int offset) {
        if (value.length() - offset < match_array.length) {
            return -1;
        }
        int    src_length   = value.length();
        int    mat_index    = 0;
        int    src_index    = offset;
        int    match_length = this.match_array.length;
        char[] match_array  = this.match_array;
        int[]  match_table  = this.match_table;
        for (; src_index < src_length; ) {
            char s_c = value.charAt(src_index);
            char m_c = match_array[mat_index];
            if (s_c == m_c) {
                mat_index++;
                src_index++;
                if (mat_index == match_length) {
                    return src_index - match_length;
                }
            } else {
                if (mat_index == 0) {
                    src_index++;
                } else {
                    // 第mat_index位不匹配，查找这以前的匹配情况（-1）
                    mat_index = match_table[mat_index - 1];
                }
            }
        }
        return -1;
    }

}
