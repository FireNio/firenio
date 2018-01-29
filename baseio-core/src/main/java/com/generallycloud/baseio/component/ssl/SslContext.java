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

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManagerFactory;

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
    
    static TrustManagerFactory buildTrustManagerFactory(X509Certificate[] certCollection,
            TrustManagerFactory trustManagerFactory)
            throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, null);
        int i = 1;
        for (X509Certificate cert : certCollection) {
            String alias = Integer.toString(i);
            ks.setCertificateEntry(alias, cert);
            i++;
        }
        // Set up trust manager factory to use our key store.
        if (trustManagerFactory == null) {
            trustManagerFactory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        }
        trustManagerFactory.init(ks);
        return trustManagerFactory;
    }
    
    static KeyManagerFactory buildKeyManagerFactory(X509Certificate[] certChain, PrivateKey key,
            String keyPassword, KeyManagerFactory kmf) throws UnrecoverableKeyException,
            KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }
        return buildKeyManagerFactory(certChain, algorithm, key, keyPassword, kmf);
    }

    static KeyManagerFactory buildKeyManagerFactory(X509Certificate[] certChainFile,
            String keyAlgorithm, PrivateKey key, String keyPassword, KeyManagerFactory kmf)
            throws KeyStoreException, NoSuchAlgorithmException, IOException, CertificateException,
            UnrecoverableKeyException {
        char[] keyPasswordChars = keyPassword == null ? "".toCharArray()
                : keyPassword.toCharArray();
        KeyStore ks = buildKeyStore(certChainFile, key, keyPasswordChars);
        // Set up key manager factory to use our key store
        if (kmf == null) {
            kmf = KeyManagerFactory.getInstance(keyAlgorithm);
        }
        kmf.init(ks, keyPasswordChars);

        return kmf;
    }
    
    static KeyStore buildKeyStore(X509Certificate[] certChain, PrivateKey key,
            char[] keyPasswordChars)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, null);
        ks.setKeyEntry("key", key, keyPasswordChars, certChain);
        return ks;
    }

}
