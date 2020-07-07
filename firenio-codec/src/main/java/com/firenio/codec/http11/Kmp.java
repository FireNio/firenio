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
public class Kmp {

    private int    m_len;
    private char[] m_array;
    private int[]  m_table;

    public Kmp(String value) {
        if (Util.isNullOrBlank(value)) {
            throw new IllegalArgumentException("null value");
        }
        this.m_len = value.length();
        this.m_array = value.toCharArray();
        this.m_table = new int[value.length() - 1];
        int length = value.length() - 1;
        // 只需要计算长度减一位的部分匹配表，因为最后一位如果也匹配整个字符串就匹配了
        // 直接从第二位开始比较
        for (int i = 1; i < length; i++) {
            m_table[i] = init_table(value, i + 1);
        }
    }

    public static void main(String[] args) {

        String src   = "ABC ABCA ABCABA";
        String match = "ABCAB";
        src = "AABAABAABC";
        match = "AABAABC";
        Kmp kmp  = new Kmp(match);
        int find = 0;
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
        if (value.length() - offset < m_len) {
            return -1;
        }
        int    s_len    = value.length();
        int    m_index  = 0;
        int    s_index  = offset;
        int    m_len    = this.m_len;
        char[] m_array  = this.m_array;
        int[]  m_table  = this.m_table;
        int    s_len_s1 = s_len - m_len;
        for (; s_index < s_len_s1; ) {
            char s_c = value.charAt(s_index);
            char m_c = m_array[m_index];
            if (s_c == m_c) {
                m_index++;
                s_index++;
                if (m_index == m_len) {
                    return s_index - m_len;
                }
            } else {
                if (m_index == 0) {
                    s_index++;
                } else {
                    // 第mat_index位不匹配，查找这以前的匹配情况（-1）
                    m_index = m_table[m_index - 1];
                }
            }
        }
        for (; s_index < s_len; ) {
            char s_c = value.charAt(s_index);
            char m_c = m_array[m_index];
            if (s_c == m_c) {
                m_index++;
                s_index++;
                if (m_index == m_len) {
                    return s_index - m_len;
                }
            } else {
                if (m_index == 0) {
                    s_index++;
                } else {
                    // 第mat_index位不匹配，查找这以前的匹配情况（-1）
                    m_index = m_table[m_index - 1];
                }
                if (s_len - s_index < m_len - m_index) {
                    break;
                }
            }
        }
        return -1;
    }

}
