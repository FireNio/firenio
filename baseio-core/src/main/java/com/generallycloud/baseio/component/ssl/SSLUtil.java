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
import java.io.IOException;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.component.ssl.ApplicationProtocolConfig.Protocol;
import com.generallycloud.baseio.component.ssl.ApplicationProtocolConfig.SelectedListenerFailureBehavior;
import com.generallycloud.baseio.component.ssl.ApplicationProtocolConfig.SelectorFailureBehavior;
import com.generallycloud.baseio.log.DebugUtil;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class SSLUtil {

    private static SslContext sslContext;

    private static Logger     logger = LoggerFactory.getLogger(SSLUtil.class);

    public synchronized static SslContext initServer(File privateKey, File certificate)
            throws IOException {
        if (sslContext == null) {
            doInit(privateKey, certificate);
        }
        return sslContext;
    }

    public synchronized static SslContext initServerHttp2(File privateKey, File certificate)
            throws IOException {
        if (sslContext == null) {
            doInitHttp2(privateKey, certificate);
        }
        return sslContext;
    }

    public synchronized static SslContext initClient() {
        if (sslContext == null) {
            try {
                sslContext = SslContextBuilder.forClient().build();
            } catch (SSLException e) {
                DebugUtil.debug(e);
            }
        }
        return sslContext;
    }

    private static void doInit(File privateKey, File certificate) throws IOException {

        LoggerUtil.prettyLog(logger, "load certificate public  key: {}",
                certificate.getCanonicalPath());
        LoggerUtil.prettyLog(logger, "load certificate private key: {}",
                privateKey.getCanonicalPath());

        sslContext = SslContextBuilder.forServer(certificate, privateKey).build();
    }

    private static void doInitHttp2(File privateKey, File certificate) throws IOException {

        LoggerUtil.prettyLog(logger, "load certificate public key: {}",
                certificate.getCanonicalPath());
        LoggerUtil.prettyLog(logger, "load certificate private key: {}",
                privateKey.getCanonicalPath());

        sslContext = SslContextBuilder.forServer(certificate, privateKey)
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

    }

    public static SSLEngine getSslEngine() {
        return sslContext.newEngine();
    }

    public static SslContext getSslContext() {
        return sslContext;
    }

}
