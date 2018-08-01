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

import javax.net.ssl.SSLException;

import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.component.ssl.ApplicationProtocolConfig.Protocol;
import com.generallycloud.baseio.component.ssl.ApplicationProtocolConfig.SelectedListenerFailureBehavior;
import com.generallycloud.baseio.component.ssl.ApplicationProtocolConfig.SelectorFailureBehavior;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class SSLUtil {

    private static boolean ENABLE_SSL = false;

    private static Logger  logger     = LoggerFactory.getLogger(SSLUtil.class);

    public synchronized static SslContext initServer(File privateKey, File certificate)
            throws IOException {
        return doInit(privateKey, certificate);
    }

    public synchronized static SslContext initServer(File storeFile, String storePassword,
            String alias, String keyPassword) throws SSLException, FileNotFoundException {
        FileInputStream storeInput = new FileInputStream(storeFile);
        try {
            return SslContextBuilder.forServer(storeInput, storePassword, alias, keyPassword)
                    .build();
        } finally {
            CloseUtil.close(storeInput);
        }
    }

    public synchronized static SslContext initServerHttp2(File privateKey, File certificate)
            throws IOException {
        return doInitHttp2(privateKey, certificate);
    }

    public synchronized static SslContext initClient(boolean trustAll) throws SSLException {
        return SslContextBuilder.forClient(trustAll).build();
    }

    private static SslContext doInit(File privateKey, File certificate) throws IOException {
        LoggerUtil.prettyLog(logger, "load certificate public  key: {}",
                certificate.getCanonicalPath());
        LoggerUtil.prettyLog(logger, "load certificate private key: {}",
                privateKey.getCanonicalPath());
        FileInputStream keyInput = new FileInputStream(privateKey);
        FileInputStream certInput = new FileInputStream(certificate);
        try {
            return SslContextBuilder.forServer(certInput, keyInput, null).build();
        } finally {
            CloseUtil.close(keyInput);
            CloseUtil.close(certInput);
        }
    }

    private static SslContext doInitHttp2(File privateKey, File certificate) throws IOException {
        LoggerUtil.prettyLog(logger, "load certificate public key: {}",
                certificate.getCanonicalPath());
        LoggerUtil.prettyLog(logger, "load certificate private key: {}",
                privateKey.getCanonicalPath());
        FileInputStream keyInput = new FileInputStream(privateKey);
        FileInputStream certInput = new FileInputStream(certificate);
        try {
            return SslContextBuilder.forServer(certInput, keyInput, null)
                    .applicationProtocolConfig(new ApplicationProtocolConfig(Protocol.ALPN,
                            // NO_ADVERTISE is currently the
                            // only mode supported by both
                            // OpenSsl and JDK providers.
                            SelectorFailureBehavior.NO_ADVERTISE,
                            // ACCEPT is currently the only
                            // mode supported by both OpenSsl
                            // and JDK providers.
                            SelectedListenerFailureBehavior.ACCEPT, ApplicationProtocolNames.HTTP_2,
                            ApplicationProtocolNames.HTTP_1_1))
                    .build();
        } finally {
            CloseUtil.close(keyInput);
            CloseUtil.close(certInput);
        }
    }

    public static boolean isENABLE_SSL() {
        return ENABLE_SSL;
    }

    public static void setENABLE_SSL(boolean ENABLE_SSL) {
        SSLUtil.ENABLE_SSL = SSLUtil.ENABLE_SSL || ENABLE_SSL;
    }

}
