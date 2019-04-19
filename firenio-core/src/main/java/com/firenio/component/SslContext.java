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
package com.firenio.component;

import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSessionContext;

import com.firenio.Options;
import com.firenio.log.Logger;
import com.firenio.log.LoggerFactory;

//ref from netty && undertow
public final class SslContext {

    public static final List<String> ENABLED_CIPHERS;
    public static final String[]     ENABLED_PROTOCOLS;
    public static final boolean      OPENSSL_AVAILABLE;
    public static final int          SSL_PACKET_BUFFER_SIZE;
    public static final int          SSL_UNWRAP_BUFFER_SIZE;
    public static final Set<String>  SUPPORTED_CIPHERS;
    static final        Logger       logger = LoggerFactory.getLogger(SslContext.class);

    static {
        try {
            CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new IllegalStateException("unable to instance X.509 CertificateFactory", e);
        }
        boolean testOpenSsl = false;
        try {
            if (Options.isEnableOpenssl()) {
                Class.forName("org.wildfly.openssl.OpenSSLProvider");
                org.wildfly.openssl.OpenSSLProvider.register();
                testOpenSsl = true;
            }
        } catch (Exception | Error e) {
        }
        OPENSSL_AVAILABLE = testOpenSsl;
        SSLContext context;
        try {
            context = newSSLContext();
            context.init(null, null, null);
        } catch (Exception e) {
            throw new Error("failed to initialize the default SSL context", e);
        }
        SSL_UNWRAP_BUFFER_SIZE = Options.getSslUnwrapBufferSize(1024 * 256);
        SSLEngine engine = context.createSSLEngine();
        SSL_PACKET_BUFFER_SIZE = engine.getSession().getPacketBufferSize();
        // Choose the sensible default list of protocols.
        final String[] supportedProtocols    = engine.getSupportedProtocols();
        Set<String>    supportedProtocolsSet = new HashSet<>(supportedProtocols.length);
        supportedProtocolsSet.addAll(Arrays.asList(supportedProtocols));
        List<String> protocols = new ArrayList<>();
        addIfSupported(supportedProtocolsSet, protocols, "TLSv1.3", "TLSv1.2", "TLSv1.1", "TLSv1");
        if (!protocols.isEmpty()) {
            ENABLED_PROTOCOLS = protocols.toArray(new String[0]);
        } else {
            ENABLED_PROTOCOLS = engine.getEnabledProtocols();
        }
        // Choose the sensible default list of cipher suites.
        final String[] supportedCiphers = engine.getSupportedCipherSuites();
        SUPPORTED_CIPHERS = new HashSet<>(supportedCiphers.length);
        Collections.addAll(SUPPORTED_CIPHERS, supportedCiphers);
        List<String> enabledCiphers = new ArrayList<>();
        addIfSupported(SUPPORTED_CIPHERS, enabledCiphers,
                // GCM (Galois/Counter Mode) requires JDK 8.
                "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384", "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
                // AES256 requires JCE unlimited strength jurisdiction
                // policy files.
                "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
                // GCM (Galois/Counter Mode) requires JDK 8.
                "TLS_RSA_WITH_AES_128_GCM_SHA256", "TLS_RSA_WITH_AES_128_CBC_SHA",
                // AES256 requires JCE unlimited strength jurisdiction
                // policy files.
                "TLS_RSA_WITH_AES_256_CBC_SHA");

        if (enabledCiphers.isEmpty()) {
            // Use the default from JDK as fallback.
            for (String cipher : engine.getEnabledCipherSuites()) {
                if (cipher.contains("_RC4_")) {
                    continue;
                }
                enabledCiphers.add(cipher);
            }
        }
        ENABLED_CIPHERS = Collections.unmodifiableList(enabledCiphers);
    }

    private final String[] applicationProtocols;

    private final String[] cipherSuites;

    private final ClientAuth clientAuth;

    private final boolean      isServer;
    private final SSLContext   sslContext;
    private final List<String> unmodifiableCipherSuites;

    SslContext(SSLContext sslContext, boolean isServer, List<String> ciphers, ClientAuth clientAuth, String[] applicationProtocols) throws SSLException {
        this.applicationProtocols = applicationProtocols;
        this.clientAuth = clientAuth;
        this.cipherSuites = filterCipherSuites(ciphers, ENABLED_CIPHERS, SUPPORTED_CIPHERS);
        this.unmodifiableCipherSuites = Collections.unmodifiableList(Arrays.asList(cipherSuites));
        this.sslContext = sslContext;
        this.isServer = isServer;
        if (applicationProtocols != null && !OPENSSL_AVAILABLE) {
            throw new SSLException("applicationProtocols enabled but openssl not available");
        }
    }

    private static void addIfSupported(Set<String> supported, List<String> enabled, String... names) {
        for (String n : names) {
            if (supported.contains(n)) {
                enabled.add(n);
            }
        }
    }

    public static int getPacketBufferSize() {
        return SSL_PACKET_BUFFER_SIZE;
    }

    static SSLContext newSSLContext() throws NoSuchAlgorithmException {
        if (OPENSSL_AVAILABLE) {
            return SSLContext.getInstance("openssl.TLS");
        } else {
            return SSLContext.getInstance("TLS");
        }
    }

    public final List<String> cipherSuites() {
        return unmodifiableCipherSuites;
    }

    private SSLEngine configureEngine(SSLEngine engine) {
        engine.setEnabledCipherSuites(cipherSuites);
        engine.setEnabledProtocols(ENABLED_PROTOCOLS);
        engine.setUseClientMode(isClient());
        if (isServer()) {
            if (clientAuth == ClientAuth.OPTIONAL) {
                engine.setWantClientAuth(true);
            } else if (clientAuth == ClientAuth.REQUIRE) {
                engine.setNeedClientAuth(true);
            }
        }
        if (applicationProtocols == null) {
            return engine;
        }
        ((org.wildfly.openssl.OpenSSLEngine) engine).setApplicationProtocols(applicationProtocols);
        return engine;
    }

    private String[] filterCipherSuites(List<String> ciphers, List<String> defaultCiphers, Set<String> supportedCiphers) {
        if (ciphers == null) {
            return defaultCiphers.toArray(new String[0]);
        } else {
            List<String> newCiphers = new ArrayList<>();
            for (String c : ciphers) {
                if (c == null) {
                    break;
                }
                if (supportedCiphers.contains(c)) {
                    newCiphers.add(c);
                }
            }
            return newCiphers.toArray(new String[0]);
        }
    }

    public SSLContext getSSLContext() {
        return sslContext;
    }

    public final boolean isClient() {
        return !isServer();
    }

    public final boolean isServer() {
        return isServer;
    }

    public final SSLEngine newEngine(String peerHost, int peerPort) {
        return configureEngine(sslContext.createSSLEngine(peerHost, peerPort));
    }

    public final long sessionCacheSize() {
        return sessionContext().getSessionCacheSize();
    }

    public final SSLSessionContext sessionContext() {
        if (isServer()) {
            return sslContext.getServerSessionContext();
        } else {
            return sslContext.getClientSessionContext();
        }
    }

    public final long sessionTimeout() {
        return sessionContext().getSessionTimeout();
    }

    public enum ClientAuth {

        NONE,

        OPTIONAL,

        REQUIRE
    }

}
