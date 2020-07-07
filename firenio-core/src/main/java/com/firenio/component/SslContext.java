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

import java.lang.reflect.Method;
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
import com.firenio.common.Unsafe;
import com.firenio.log.Logger;
import com.firenio.log.LoggerFactory;

//ref from netty && undertow
public final class SslContext {

    public static final List<String> DEFAULT_CIPHERS;
    public static final List<String> DEFAULT_PROTOCOLS;
    public static final boolean      OPENSSL_AVAILABLE;
    public static final int          SSL_PACKET_BUFFER_SIZE;
    public static final int          SSL_UNWRAP_BUFFER_SIZE;
    public static final Set<String>  SUPPORTED_CIPHERS;
    public static final Set<String>  SUPPORTED_PROTOCOLS;
    static final        Logger       logger = LoggerFactory.getLogger(SslContext.class);

    static {
        try {
            CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new IllegalStateException("unable to instance X.509 CertificateFactory", e);
        }
        boolean testOpenSsl = false;
        try {
            if (Options.isEnableOpenssl() && Unsafe.UNSAFE_AVAILABLE) {
                registerOpenSsl();
                testOpenSsl = true;
            }
        } catch (Throwable e) {
        }
        OPENSSL_AVAILABLE = testOpenSsl;
        SSLContext context;
        try {
            context = newSSLContext();
            context.init(null, null, null);
        } catch (Exception e) {
            throw new Error("failed to initialize the default SSL context", e);
        }
        SSLEngine engine = context.createSSLEngine();
        SSL_UNWRAP_BUFFER_SIZE = Options.getSslUnwrapBufferSize(1024 * 256);
        SSL_PACKET_BUFFER_SIZE = engine.getSession().getPacketBufferSize();

        // Choose the sensible default list of protocols.
        final String[] supportedProtocols = engine.getSupportedProtocols();
        SUPPORTED_PROTOCOLS = new HashSet<>(supportedProtocols.length);
        Collections.addAll(SUPPORTED_PROTOCOLS, supportedProtocols);

        // Choose the sensible default list of cipher suites.
        final String[] supportedCiphers = engine.getSupportedCipherSuites();
        SUPPORTED_CIPHERS = new HashSet<>(supportedCiphers.length);
        Collections.addAll(SUPPORTED_CIPHERS, supportedCiphers);

        // default protocols
        List<String> defaultProtocols = new ArrayList<>();
        defaultProtocols.add("TLSv1.3");
        defaultProtocols.add("TLSv1.2");
        defaultProtocols.add("TLSv1.1");

        // ECDHE == PFS
        List<String> defaultCiphers = new ArrayList<>();
        // GCM (Galois/Counter Mode) requires JDK 8.
        defaultCiphers.add("TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384");
        defaultCiphers.add("TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256");
        defaultCiphers.add("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
        defaultCiphers.add("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA");
        // AES256 requires JCE unlimited strength jurisdiction policy files.
        defaultCiphers.add("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA");
        // GCM (Galois/Counter Mode) requires JDK 8.
        defaultCiphers.add("TLS_RSA_WITH_AES_128_GCM_SHA256");
        defaultCiphers.add("TLS_RSA_WITH_AES_128_CBC_SHA");
        // AES256 requires JCE unlimited strength jurisdiction policy files.
        defaultCiphers.add("TLS_RSA_WITH_AES_256_CBC_SHA");

        DEFAULT_PROTOCOLS = Collections.unmodifiableList(defaultProtocols);
        DEFAULT_CIPHERS = Collections.unmodifiableList(defaultCiphers);
    }

    private final String[]     applicationProtocols;
    private final String[]     cipherSuites;
    private final String[]     protocols;
    private final ClientAuth   clientAuth;
    private final boolean      isServer;
    private final SSLContext   sslContext;
    private final List<String> unmodifiableCipherSuites;
    private final List<String> unmodifiableProtocols;

    SslContext(SSLContext sslContext, boolean isServer, List<String> protocols, List<String> ciphers, ClientAuth clientAuth, String[] applicationProtocols) throws SSLException {
        this.applicationProtocols = applicationProtocols;
        this.clientAuth = clientAuth;
        this.cipherSuites = filterCipherSuites(ciphers, DEFAULT_CIPHERS, SUPPORTED_CIPHERS);
        this.protocols = filterCipherSuites(protocols, DEFAULT_PROTOCOLS, SUPPORTED_PROTOCOLS);
        this.unmodifiableCipherSuites = Collections.unmodifiableList(Arrays.asList(cipherSuites));
        this.unmodifiableProtocols = Collections.unmodifiableList(Arrays.asList(this.protocols));
        this.sslContext = sslContext;
        this.isServer = isServer;
        if (applicationProtocols != null && !OPENSSL_AVAILABLE) {
            throw new SSLException("applicationProtocols enabled but openssl not available");
        }
    }

    private static void registerOpenSsl() throws Exception {
        String      clazzName = "org.wildfly.openssl.OpenSSLProvider";
        ClassLoader cl        = SslContext.class.getClassLoader();
        Class       clazz     = Class.forName(clazzName, false, cl);
        Method      method    = clazz.getDeclaredMethod("register");
        method.setAccessible(true);
        method.invoke(null);
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

    public final List<String> getCipherSuites() {
        return unmodifiableCipherSuites;
    }

    public final List<String> getProtocols() {
        return unmodifiableProtocols;
    }

    private SSLEngine configureEngine(SSLEngine engine) {
        engine.setEnabledCipherSuites(cipherSuites);
        engine.setEnabledProtocols(protocols);
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
        List<String> filter_ciphers = ciphers;
        if (filter_ciphers == null) {
            filter_ciphers = defaultCiphers;
        }
        List<String> newCiphers = new ArrayList<>();
        for (String c : filter_ciphers) {
            if (supportedCiphers.contains(c)) {
                newCiphers.add(c);
            }
        }
        return newCiphers.toArray(new String[0]);
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
