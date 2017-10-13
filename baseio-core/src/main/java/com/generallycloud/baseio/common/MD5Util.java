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

public class MD5Util {

    private static MD5Util instance    = new MD5Util();

    private MD5Util() {}

    public static MD5Util get() {
        return instance;
    }

    public String get16(String value) {
        return get16(value, Encoding.UTF8);
    }

    public String get16(String value, Charset encoding) {
        return get32(value.getBytes()).substring(8, 24);
    }

    public String get32(String value) {
        return get32(value, Encoding.UTF8);
    }

    public String get32(String value, Charset encoding) {
        return get32(value.getBytes()).toString();
    }

    public String get32(byte[] array) {
        return get32(array, 0, array.length);
    }

    public String get32(byte[] array, int off, int len) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(array, off, len);
            return MathUtil.bytes2HexString(md5.digest());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
