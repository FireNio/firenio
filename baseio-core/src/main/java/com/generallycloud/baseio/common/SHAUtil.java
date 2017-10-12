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

        System.out.println(BASE64Util.byteArrayToBase64(SHA(ss)));

        System.out.println(BASE64Util.byteArrayToBase64(SHA1(ss)));
    }

    public static byte[] SHA(byte[] decript) {
        return doSHA(decript, "SHA");
    }

    public static byte[] SHA(String decript) {
        return SHA(decript, Encoding.UTF8);
    }

    public static byte[] SHA(String decript, Charset encoding) {
        return doSHA(decript.getBytes(encoding), "SHA");
    }

    public static byte[] SHA1(byte[] decript) {
        return doSHA(decript, "SHA-1");
    }

    public static byte[] SHA1(String decript) {
        return SHA1(decript, Encoding.UTF8);
    }

    public static byte[] SHA1(String decript, Charset encoding) {
        return doSHA(decript.getBytes(encoding), "SHA-1");
    }

}
