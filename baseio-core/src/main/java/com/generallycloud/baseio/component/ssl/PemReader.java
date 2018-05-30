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
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import com.generallycloud.baseio.common.BASE64Util;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.Encoding;
import com.generallycloud.baseio.common.FileUtil;

final class PemReader {

    static List<byte[]> readCertificates(File file) throws CertificateException {
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

    static List<byte[]> readCertificates(InputStream in) throws CertificateException {
        List<String> ls;
        try {
            ls = FileUtil.readLines(in, Encoding.UTF8);
        } catch (IOException e) {
            throw new CertificateException("failed to read certificate input stream", e);
        }
        List<byte[]> certs = new ArrayList<>();
        StringBuilder b = new StringBuilder();
        int readEnd = 0;

        for (String s : ls) {
            if (s.startsWith("----")) {
                readEnd++;
                if (readEnd == 2) {
                    byte[] data = BASE64Util.base64ToByteArray(b.toString());
                    certs.add(data);
                    readEnd = 0;
                    b.setLength(0);
                }
                continue;
            }
            b.append(s.trim().replace("\r", ""));
        }
        if (certs.isEmpty()) {
            throw new CertificateException("found no certificates in input stream");
        }
        return certs;
    }

    public static void main(String[] args) throws CertificateException {
        List<byte[]> res = readCertificates(FileUtil.readInputStreamByCls("full_chain.pem"));
        System.out.println(res);
    }

}
