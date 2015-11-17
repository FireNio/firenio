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
package test.others.ssl;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.firenio.common.FileUtil;
import com.firenio.common.Util;

public class SSLSocketClient {

    public static void main(String[] args) throws Exception {

        SSLContext context = SSLContext.getInstance("TLS");
        X509TrustManager x509m = new X509TrustManager() {

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType)
                    throws CertificateException {
                System.out.println("checkClientTrusted......");

            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType)
                    throws CertificateException {
                System.out.println("checkServerTrusted......");
                //When I use jdkssl the chain'value is all of my server configured.
                //But when openssl the chain'value only one
                //Why this happened? is my server configuration incorrect or this is openssl's feature?
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                System.out.println("getAcceptedIssuers......");
                return null;
            }
        };
        context.init(null, new TrustManager[] { x509m }, new SecureRandom());

        SSLSocketFactory factory = context.getSocketFactory();
        SSLSocket s = (SSLSocket) factory.createSocket("192.168.133.134", 1443);
        System.out.println("ok");

        OutputStream output = s.getOutputStream();
        InputStream input = s.getInputStream();

        int length = 1;
        StringBuilder b = new StringBuilder(length + 1);
        for (int i = 0; i < length; i++) {
            b.append('a');
        }
        b.append('\n');

        output.write(b.toString().getBytes());
        System.out.println("sent: alert");
        output.flush();

        byte[] buf = new byte[length + 200];
        int len = input.read(buf);

        Util.close(output);
        Util.close(input);
        String str = new String(buf, 0, len);
        FileUtil.writeByCls("test.txt", str, false);
        System.out.println("received:" + str);
    }
}
