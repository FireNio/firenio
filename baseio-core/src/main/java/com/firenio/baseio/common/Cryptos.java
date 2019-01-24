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

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author wangkai
 *
 */
public class Cryptos {

    /**
     * This array is the analogue of base64ToInt, but for the nonstandard
     * variant that avoids the use of uppercase alphabetic characters.
     */
    private static final byte altBase64ToInt[] = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0,
            1, 2, 3, 4, 5, 6, 7, 8, -1, 62, 9, 10, 11, -1, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61,
            12, 13, 14, -1, 15, 63, 16, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 17, -1, 18, 19, 21, 20, 26, 27, 28, 29, 30,
            31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 22,
            23, 24, 25 };

    /**
     * This array is a lookup table that translates unicode characters drawn
     * from the "Base64 Alphabet" (as specified in Table 1 of RFC 2045) into
     * their 6-bit positive integer equivalents. Characters that are not in the
     * Base64 alphabet but fall within the bounds of the array are translated
     * to -1.
     */
    private static final byte base64ToInt[]    = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59,
            60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
            15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30,
            31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51 };

    /**
     * This array is a lookup table that translates 6-bit positive integer
     * index values into their "Alternate Base64 Alphabet" equivalents. This is
     * NOT the real Base64 Alphabet as per in Table 1 of RFC 2045. This
     * alternate alphabet does not use the capital letters. It is designed for
     * use in environments where "case folding" occurs.
     */
    private static final char intToAltBase64[] = { '!', '"', '#', '$', '%', '&', '\'', '(', ')',
            ',', '-', '.', ':', ';', '<', '>', '@', '[', ']', '^', '`', '_', '{', '|', '}', '~',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
            'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', '+', '?' };

    /**
     * This array is a lookup table that translates 6-bit positive integer
     * index values into their "Base64 Alphabet" equivalents as specified in
     * Table 1 of RFC 2045.
     */
    private static final char intToBase64[]    = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a',
            'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
            's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8',
            '9', '+', '/' };

    static final ThreadLocal<Cryptos> mdh = new ThreadLocal<Cryptos>() {

        protected Cryptos initialValue() {
            return new Cryptos();
        }
    };

    private Map<String, MessageDigest> digests = new HashMap<>();

    private MessageDigest get0(String algorithm) {
        MessageDigest d = digests.get(algorithm);
        if (d == null) {
            try {
                d = MessageDigest.getInstance(algorithm);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            digests.put(algorithm, d);
            return d;
        }
        d.reset();
        return d;
    }

    public static byte[] aes_de(byte[] content, byte[] password) throws Exception {
        // 创建AES的Key生产者
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(password);
        kgen.init(128, random);
        // 根据用户密码，生成一个密钥
        SecretKey secretKey = kgen.generateKey();
        // 返回基本编码格式的密钥
        byte[] enCodeFormat = secretKey.getEncoded();
        // 转换为AES专用密钥
        SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
        // 创建密码器
        Cipher cipher = Cipher.getInstance("AES");
        // 初始化为解密模式的密码器
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(content);
    }

    public static String aes_de(String content, String password) throws Exception {
        return aes_de(content, password, Util.UTF8);
    }

    public static String aes_de(String content, String password, Charset charset) throws Exception {
        byte[] contentBytes = ByteUtil.getBytesFromHexString(content);
        byte[] passowrdBytes = password.getBytes(charset);
        byte[] res = aes_de(contentBytes, passowrdBytes);
        return new String(res, charset);
    }

    public static byte[] aes_en(byte[] content, byte[] password) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");// 创建AES的Key生产者
        // 利用用户密码作为随机数初始化出,128位的key生产者
        //加密没关系，SecureRandom是生成安全随机数序列，password.getBytes()是种子，只要种子相同，序列就一样，所以解密只要有password就行
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(password);
        kgen.init(128, random);
        SecretKey secretKey = kgen.generateKey();// 根据用户密码，生成一个密钥
        // 返回基本编码格式的密钥，如果此密钥不支持编码，则返回null
        byte[] enCodeFormat = secretKey.getEncoded();
        // 转换为AES专用密钥
        SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
        // 创建密码器
        Cipher cipher = Cipher.getInstance("AES");
        // 初始化为加密模式的密码器
        cipher.init(Cipher.ENCRYPT_MODE, key);
        // 加密
        return cipher.doFinal(content);
    }

    public static String aes_en(String content, String password) throws Exception {
        return aes_en(content, password, Util.UTF8);
    }

    public static String aes_en(String content, String password, Charset charset) throws Exception {
        byte[] contentBytes = content.getBytes(charset);
        byte[] passwordBytes = password.getBytes(charset);
        byte[] res = aes_en(contentBytes, passwordBytes);
        return ByteUtil.getHexString(res);
    }

    /**
     * Translates the specified "alternate representation" Base64 string into a
     * byte array.
     */
    public static byte[] alt_base64_de(String s) {
        return base64_de(s, true);
    }

    /**
     * Translates the specified byte array into an "alternate representation"
     * Base64 string. This non-standard variant uses an alphabet that does not
     * contain the uppercase alphabetic characters, which makes it suitable for
     * use in situations where case-folding occurs.
     */
    public static String alt_base64_en(byte[] a) {
        return base64_en(a, true);
    }

    /**
     * Translates the specified Base64 string (as per Preferences.get(byte[]))
     * into a byte array.
     */
    public static byte[] base64_de(String s) {
        return base64_de(s, false);
    }

    private static byte[] base64_de(String s, boolean alternate) {
        byte[] alphaToInt = (alternate ? altBase64ToInt : base64ToInt);
        int sLen = s.length();
        int numGroups = sLen / 4;
        if (4 * numGroups != sLen) {
            throw new IllegalArgumentException(
                    "String length must be a multiple of four. len=" + sLen);
        }
        int missingBytesInLastGroup = 0;
        int numFullGroups = numGroups;
        if (sLen != 0) {
            if (s.charAt(sLen - 1) == '=') {
                missingBytesInLastGroup++;
                numFullGroups--;
            }
            if (s.charAt(sLen - 2) == '=') {
                missingBytesInLastGroup++;
            }
        }
        byte[] result = new byte[3 * numGroups - missingBytesInLastGroup];

        // Translate all full groups from base64 to byte array elements
        int inCursor = 0, outCursor = 0;
        for (int i = 0; i < numFullGroups; i++) {
            int ch0 = base64toInt(s.charAt(inCursor++), alphaToInt);
            int ch1 = base64toInt(s.charAt(inCursor++), alphaToInt);
            int ch2 = base64toInt(s.charAt(inCursor++), alphaToInt);
            int ch3 = base64toInt(s.charAt(inCursor++), alphaToInt);
            result[outCursor++] = (byte) ((ch0 << 2) | (ch1 >> 4));
            result[outCursor++] = (byte) ((ch1 << 4) | (ch2 >> 2));
            result[outCursor++] = (byte) ((ch2 << 6) | ch3);
        }

        // Translate partial group, if present
        if (missingBytesInLastGroup != 0) {
            int ch0 = base64toInt(s.charAt(inCursor++), alphaToInt);
            int ch1 = base64toInt(s.charAt(inCursor++), alphaToInt);
            result[outCursor++] = (byte) ((ch0 << 2) | (ch1 >> 4));

            if (missingBytesInLastGroup == 1) {
                int ch2 = base64toInt(s.charAt(inCursor++), alphaToInt);
                result[outCursor++] = (byte) ((ch1 << 4) | (ch2 >> 2));
            }
        }
        // assert inCursor == s.length()-missingBytesInLastGroup;
        // assert outCursor == result.length;
        return result;
    }

    /**
     * Translates the specified byte array into a Base64 string as per
     * Preferences.put(byte[]).
     */
    public static String base64_en(byte[] a) {
        return base64_en(a, false);
    }

    private static String base64_en(byte[] a, boolean alternate) {
        int aLen = a.length;
        int numFullGroups = aLen / 3;
        int numBytesInPartialGroup = aLen - 3 * numFullGroups;
        int resultLen = 4 * ((aLen + 2) / 3);
        StringBuilder result = new StringBuilder(resultLen);
        char[] intToAlpha = (alternate ? intToAltBase64 : intToBase64);

        // Translate all full groups from byte array elements to Base64
        int inCursor = 0;
        for (int i = 0; i < numFullGroups; i++) {
            int byte0 = a[inCursor++] & 0xff;
            int byte1 = a[inCursor++] & 0xff;
            int byte2 = a[inCursor++] & 0xff;
            result.append(intToAlpha[byte0 >> 2]);
            result.append(intToAlpha[(byte0 << 4) & 0x3f | (byte1 >> 4)]);
            result.append(intToAlpha[(byte1 << 2) & 0x3f | (byte2 >> 6)]);
            result.append(intToAlpha[byte2 & 0x3f]);
        }

        // Translate partial group if present
        if (numBytesInPartialGroup != 0) {
            int byte0 = a[inCursor++] & 0xff;
            result.append(intToAlpha[byte0 >> 2]);
            if (numBytesInPartialGroup == 1) {
                result.append(intToAlpha[(byte0 << 4) & 0x3f]);
                result.append("==");
            } else {
                // assert numBytesInPartialGroup == 2;
                int byte1 = a[inCursor++] & 0xff;
                result.append(intToAlpha[(byte0 << 4) & 0x3f | (byte1 >> 4)]);
                result.append(intToAlpha[(byte1 << 2) & 0x3f]);
                result.append('=');
            }
        }
        // assert inCursor == a.length;
        // assert result.length() == resultLen;
        return result.toString();
    }

    /**
     * Translates the specified character, which is assumed to be in the
     * "Base 64 Alphabet" into its equivalent 6-bit positive integer.
     * 
     * @throw IllegalArgumentException or ArrayOutOfBoundsException if c is not
     *        in the Base64 Alphabet.
     */
    private static int base64toInt(char c, byte[] alphaToInt) {
        int result = alphaToInt[c];
        if (result < 0) {
            throw new IllegalArgumentException("Illegal character " + c);
        }
        return result;
    }

    private static MessageDigest get(String algorithm) {
        return mdh.get().get0(algorithm);
    }
    
    public static String get32(String value) {
        return get32(value, Util.UTF8);
    }

    public static String get32(String value, Charset encoding) {
        return getMd5_32(value.getBytes(encoding));
    }

    public static String getMd5_16(String value) {
        return getMd5_16(value, Util.UTF8);
    }

    public static String getMd5_16(String value, Charset encoding) {
        return getMd5_32(value.getBytes(encoding)).substring(8, 24);
    }

    public static String getMd5_32(byte[] array) {
        return getMd5_32(array, 0, array.length);
    }

    public static String getMd5_32(byte[] input, int off, int len) {
        return ByteUtil.getHexString(update(input, off, len, "MD5"));
    }

    public static String getMd5_32(String value) {
        return getMd5_32(value, Util.UTF8);
    }

    public static String getMd5_32(String value, Charset encoding) {
        return getMd5_32(value.getBytes(encoding));
    }

    public static void main(String[] args) throws Exception {
        
        testAes();
        testBase64();
        testSHA1();
        testMd5();
        
    }

    public static byte[] SHA1(byte[] decript) {
        return Cryptos.update(decript, "SHA-1");
    }

    public static byte[] SHA1(String decript) {
        return SHA1(decript, Util.UTF8);
    }

    public static byte[] SHA1(String decript, Charset encoding) {
        return SHA1(decript.getBytes(encoding));
    }

    public static byte[] SHA256(byte[] decript) {
        return update(decript, "SHA-256");
    }

    public static byte[] SHA256(String decript) {
        return SHA256(decript, Util.UTF8);
    }

    public static byte[] SHA256(String decript, Charset encoding) {
        return SHA256(decript.getBytes(encoding));
    }

    public static byte[] SHA512(byte[] decript) {
        return update(decript, "SHA-512");
    }

    public static byte[] SHA512(String decript) {
        return SHA512(decript, Util.UTF8);
    }

    public static byte[] SHA512(String decript, Charset encoding) {
        return SHA512(decript.getBytes(encoding));
    }

    public static void testAes() throws Exception {

        String text = "test";
        String key = "key";
        System.out.println("TEXT:" + text);
        System.out.println("KEY:" + key);
        String mi = aes_en(text, key);
        System.out.println("MI:" + mi);
        text = aes_de(mi, key);
        System.out.println("TEXT:" + text);

    }

    public static void testBase64() {

        String s = new String(base64_de("ODg4ODg4"));
        System.out.println(s);

    }

    static void testMd5(){
        System.out.println("md5_16:"+getMd5_16("123456"));
        System.out.println("md5_32:"+getMd5_32("123456"));
    }

    public static void testSHA1() {

        String ss = "test2";
        long startTime = System.currentTimeMillis();
        int count = 1000000;
        for (int i = 0; i < count; i++) {
            ByteUtil.getHexString(SHA1(ss));
        }
        System.out.println("Time:" + (System.currentTimeMillis() - startTime));
        System.out.println(ByteUtil.getHexString(SHA1(ss)));

        System.out.println(ByteUtil.getHexString(SHA256(ss)));

        System.out.println(ByteUtil.getHexString(SHA512(ss)));
    }
    
    private static byte[] update(byte[] input, int off, int len, String algorithm) {
        MessageDigest digest = get(algorithm);
        digest.update(input, off, len);
        return digest.digest();
    }
    
    private static byte[] update(byte[] input, String algorithm) {
        return update(input, 0, input.length, algorithm);
    }

}
