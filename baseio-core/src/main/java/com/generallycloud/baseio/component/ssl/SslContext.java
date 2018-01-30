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

import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.List;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSessionContext;

import com.generallycloud.baseio.component.SocketChannelContext;

public abstract class SslContext {
    
    static {
        try {
            CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new IllegalStateException("unable to instance X.509 CertificateFactory", e);
        }
    }

    public final boolean isServer() {
        return !isClient();
    }

    public abstract boolean isClient();

    public abstract List<String> cipherSuites();

    public abstract long sessionCacheSize();

    public abstract long sessionTimeout();

    public abstract JdkApplicationProtocolNegotiator applicationProtocolNegotiator();

    public abstract SSLEngine newEngine();

    public abstract SSLEngine newEngine(String peerHost, int peerPort);

    public abstract SSLSessionContext sessionContext();

    public SslHandler newSslHandler(SocketChannelContext context) {
        return new SslHandler(context);
    }
    
}
