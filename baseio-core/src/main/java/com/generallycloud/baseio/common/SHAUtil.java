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

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHAUtil {

    public static byte[] doSHA(byte[] decript, String digestType) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance(digestType);
            digest.update(decript);
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {

        String ss = "test2";
        long startTime = System.currentTimeMillis();
        int count = 1000000;
        for (int i = 0; i < count; i++) {
            MathUtil.bytes2HexString(SHA1(ss));
        }
        System.out.println("Time:"+(System.currentTimeMillis() - startTime));
        System.out.println(MathUtil.bytes2HexString(SHA1(ss)));

        System.out.println(MathUtil.bytes2HexString(SHA256(ss)));

        System.out.println(MathUtil.bytes2HexString(SHA512(ss)));
    }

    public static byte[] SHA1(byte[] decript) {
        return doSHA(decript, "SHA-1");
    }

    public static byte[] SHA1(String decript) {
        return SHA1(decript, Encoding.UTF8);
    }

    public static byte[] SHA1(String decript, Charset encoding) {
        return SHA1(decript.getBytes(encoding));
    }

    public static byte[] SHA256(byte[] decript) {
        return doSHA(decript, "SHA-256");
    }

    public static byte[] SHA256(String decript) {
        return SHA256(decript, Encoding.UTF8);
    }

    public static byte[] SHA256(String decript, Charset encoding) {
        return SHA256(decript.getBytes(encoding));
    }

    public static byte[] SHA512(byte[] decript) {
        return doSHA(decript, "SHA-512");
    }

    public static byte[] SHA512(String decript) {
        return SHA512(decript, Encoding.UTF8);
    }

    public static byte[] SHA512(String decript, Charset encoding) {
        return SHA512(decript.getBytes(encoding));
    }

}
