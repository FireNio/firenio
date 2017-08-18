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
package com.generallycloud.baseio.component.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import com.generallycloud.baseio.common.BASE64Util;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.Encoding;
import com.generallycloud.baseio.common.FileUtil;

final class PemReader {

    static byte[][] readCertificates(File file) throws CertificateException {
        try {
            InputStream in = new FileInputStream(file);

            try {
                return readCertificates(in);
            } finally {
                CloseUtil.close(in);
            }
        } catch (FileNotFoundException e) {
            throw new CertificateException("could not find certificate file: " + file);
        }
    }

    static byte[][] readCertificates(InputStream in) throws CertificateException {
        String content;
        try {
            content = FileUtil.input2String(in, Encoding.UTF8);
        } catch (IOException e) {
            throw new CertificateException("failed to read certificate input stream", e);
        }

        String[] ls = content.split("\n");

        StringBuilder b = new StringBuilder();

        for (String s : ls) {
            if (s.startsWith("----")) {
                continue;
            }
            b.append(s.trim().replace("\r", ""));
        }

        List<byte[]> certs = new ArrayList<>();

        byte[] data = BASE64Util.base64ToByteArray(b.toString());

        certs.add(data);

        if (certs.isEmpty()) {
            throw new CertificateException("found no certificates in input stream");
        }

        return certs.toArray(new byte[][] {});
    }

    static byte[] readPrivateKey(File file) throws KeyException {
        try {
            InputStream in = new FileInputStream(file);

            try {
                return readPrivateKey(in);
            } finally {
                CloseUtil.close(in);
            }
        } catch (FileNotFoundException e) {
            throw new KeyException("could not fine key file: " + file);
        }
    }

    static byte[] readPrivateKey(InputStream in) throws KeyException {
        String content;
        try {
            content = FileUtil.input2String(in, Encoding.UTF8);
        } catch (IOException e) {
            throw new KeyException("failed to read key input stream", e);
        }

        String[] ls = content.split("\n");

        StringBuilder b = new StringBuilder();

        for (String s : ls) {
            if (s.startsWith("-----")) {
                continue;
            }
            b.append(s.trim().replace("\r", ""));
        }

        byte[] der = BASE64Util.base64ToByteArray(b.toString());
        return der;
    }

    private PemReader() {}
}
