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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import com.firenio.common.Assert;
import com.firenio.common.Cryptos;
import com.firenio.common.FileUtil;
import com.firenio.common.Util;
import com.firenio.component.SslContext.ClientAuth;

/**
 * Builder for configuring a new SslContext for creation.
 */
//ref from netty & undertow
public final class SslContextBuilder {

    private final boolean               isServer;
    private       String[]              applicationProtocols;
    private       List<String>          ciphers;
    private       List<String>          protocols;
    private       ClientAuth            clientAuth = ClientAuth.NONE;
    private       TrustType             trustType;
    private       KeyManagerFactory     keyManagerFactory;
    private       long                  sessionCacheSize;
    private       long                  sessionTimeout;
    private       TrustManagerFactory   trustManagerFactory;
    private       List<X509Certificate> rawX509Certificates;
    private       X509TrustManager      x509TrustManager;

    private SslContextBuilder(boolean isServer) {
        this.isServer = isServer;
    }

    public static SslContextBuilder forClient(boolean trustAll) {
        return new SslContextBuilder(false).trust(trustAll);
    }

    public static SslContextBuilder forServer() {
        return new SslContextBuilder(true);
    }

    static List<byte[]> readCertificates(InputStream in) throws CertificateException {
        List<String> ls;
        try {
            ls = FileUtil.readLines(in, Util.UTF8);
        } catch (IOException e) {
            throw new CertificateException("failed to read certificate input stream", e);
        }
        return readCertificates(ls);
    }

    static List<byte[]> readCertificates(List<String> ls) throws CertificateException {
        List<byte[]>  certs   = new ArrayList<>();
        StringBuilder b       = new StringBuilder();
        int           readEnd = 0;
        for (String s : ls) {
            if (s.startsWith("----")) {
                readEnd++;
                if (readEnd == 2) {
                    byte[] data = Cryptos.base64_de(b.toString());
                    certs.add(data);
                    readEnd = 0;
                    b.setLength(0);
                }
                continue;
            }
            b.append(s.trim().replace("\r", ""));
        }
        if (certs.isEmpty()) {
            throw new CertificateException("found no certificate in input stream");
        }
        return certs;
    }

    public SslContextBuilder applicationProtocols(String[] applicationProtocols) {
        this.applicationProtocols = applicationProtocols;
        return this;
    }

    public SslContext build() throws SSLException {
        SSLContext context = newSSLContext();
        return new SslContext(context, isServer, protocols, ciphers, clientAuth, applicationProtocols);
    }

    private KeyManagerFactory buildKeyManagerFactory(KeyStore ks, char[] keyPasswordChars) throws SSLException {
        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }
        // Set up key manager factory to use our key store
        try {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
            kmf.init(ks, keyPasswordChars);
            return kmf;
        } catch (Exception e) {
            throw new SSLException(e);
        }
    }

    private KeyManagerFactory buildKeyManagerFactory(X509Certificate[] certChain, PrivateKey key, String keyPassword) throws SSLException {
        if (keyPassword == null) {
            keyPassword = "";
        }
        char[]   keyPasswordChars = keyPassword.toCharArray();
        KeyStore ks               = buildKeyStore(certChain, key, keyPasswordChars);
        return buildKeyManagerFactory(ks, keyPasswordChars);
    }

    private KeyStore buildKeyStore(X509Certificate[] certChain, PrivateKey key, char[] keyPasswordChars) throws SSLException {
        try {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            ks.setKeyEntry("key", key, keyPasswordChars, certChain);
            return ks;
        } catch (Exception e) {
            throw new SSLException(e);
        }
    }

    public SslContextBuilder ciphers(List<String> ciphers) {
        needServer();
        this.ciphers = ciphers;
        return this;
    }

    public SslContextBuilder protocols(List<String> protocols) {
        needServer();
        this.protocols = protocols;
        return this;
    }

    public SslContextBuilder clientAuth(SslContext.ClientAuth clientAuth) {
        needServer();
        this.clientAuth = clientAuth;
        return this;
    }

    private PKCS8EncodedKeySpec generateKeySpec(String password, byte[] key) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException, InvalidAlgorithmParameterException {
        if (password == null) {
            return new PKCS8EncodedKeySpec(key);
        }
        EncryptedPrivateKeyInfo epki       = new EncryptedPrivateKeyInfo(key);
        SecretKeyFactory        keyFactory = SecretKeyFactory.getInstance(epki.getAlgName());
        PBEKeySpec              pbeKeySpec = new PBEKeySpec(password.toCharArray());
        SecretKey               pbeKey     = keyFactory.generateSecret(pbeKeySpec);
        Cipher                  cipher     = Cipher.getInstance(epki.getAlgName());
        cipher.init(Cipher.DECRYPT_MODE, pbeKey, epki.getAlgParameters());
        return epki.getKeySpec(cipher);
    }

    public String[] getApplicationProtocols() {
        return applicationProtocols;
    }

    private X509Certificate[] getCertificatesFromBuffers(List<byte[]> certs) throws CertificateException {
        CertificateFactory cf        = CertificateFactory.getInstance("X.509");
        X509Certificate[]  x509Certs = new X509Certificate[certs.size()];
        for (int i = 0; i < certs.size(); i++) {
            x509Certs[i] = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certs.get(i)));
        }
        return x509Certs;
    }

    private PrivateKey getPrivateKeyFromByteBuffer(byte[] encodedKey, String keyPassword) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException, KeyException, IOException {
        PKCS8EncodedKeySpec encodedKeySpec = generateKeySpec(keyPassword, encodedKey);
        try {
            return KeyFactory.getInstance("RSA").generatePrivate(encodedKeySpec);
        } catch (InvalidKeySpecException ignore) {
        }
        try {
            return KeyFactory.getInstance("DSA").generatePrivate(encodedKeySpec);
        } catch (InvalidKeySpecException ignore2) {
        }
        try {
            return KeyFactory.getInstance("EC").generatePrivate(encodedKeySpec);
        } catch (InvalidKeySpecException e) {
            throw new InvalidKeySpecException("Neither RSA, DSA nor EC worked", e);
        }
    }

    public SslContextBuilder keyManager(InputStream keyInput, InputStream certChainInput) throws SSLException {
        return keyManager(keyInput, certChainInput, null);
    }

    public SslContextBuilder keyManager(InputStream keyInput, InputStream certChainInput, String keyPassword) throws SSLException {
        needServer();
        X509Certificate[] keyCertChain;
        PrivateKey        key;
        try {
            keyCertChain = toX509Certificates(certChainInput);
        } catch (Exception e) {
            throw new IllegalArgumentException("Input stream not contain valid certificate.", e);
        } finally {
            Util.close(certChainInput);
        }
        try {
            key = toPrivateKey(keyInput, keyPassword);
        } catch (Exception e) {
            throw new IllegalArgumentException("Input stream does not contain valid private key.", e);
        } finally {
            Util.close(keyInput);
        }
        this.keyManagerFactory = buildKeyManagerFactory(keyCertChain, key, keyPassword);
        return this;
    }

    public SslContextBuilder keyManager(InputStream keystoreInput, String storePassword, String alias, String keyPassword) throws SSLException {
        needServer();
        try {
            if (keyPassword == null) {
                keyPassword = "";
            }
            char[]   keyPasswordChars = keyPassword.toCharArray();
            KeyStore keystore         = KeyStore.getInstance("JKS");
            keystore.load(keystoreInput, keyPasswordChars);
            this.keyManagerFactory = buildKeyManagerFactory(keystore, keyPasswordChars);
            return this;
        } catch (Exception e) {
            throw new SSLException(e);
        } finally {
            Util.close(keystoreInput);
        }
    }

    public SslContextBuilder keyManager(KeyManagerFactory keyManagerFactory) {
        needServer();
        this.keyManagerFactory = keyManagerFactory;
        return this;
    }

    private void needClient() {
        if (isServer) {
            throw new IllegalArgumentException("client context mode");
        }
    }

    private void needServer() {
        if (!isServer) {
            throw new IllegalArgumentException("server context mode");
        }
    }

    private SSLContext newSSLContext() throws SSLException {
        if (isServer && keyManagerFactory == null) {
            throw new SSLException("null keyManagerFactory");
        }
        try {
            SSLContext     ctx = SslContext.newSSLContext();
            TrustManager[] tms = null;
            KeyManager[]   kms = null;
            if (isServer) {
                kms = keyManagerFactory.getKeyManagers();
            } else {
                switch (trustType) {
                    case ALL:
                        tms = new X509TrustManager[]{new TrustAllX509TrustManager()};
                        break;
                    case TrustManagerFactory:
                        tms = trustManagerFactory.getTrustManagers();
                        break;
                    case X509TrustManager:
                        tms = new X509TrustManager[]{x509TrustManager};
                        break;
                    case X509Certificate:
                        if (rawX509Certificates.size() == 1) {
                            tms = new X509TrustManager[]{new TrustOneX509TrustManager(rawX509Certificates.get(0))};
                        } else {
                            X509Certificate[] cs = rawX509Certificates.toArray(new X509Certificate[0]);
                            tms = new X509TrustManager[]{new TrustX509TrustManager(cs)};
                        }
                        break;
                    default:
                        if (!isServer) {
                            throw new SSLException("did not trust anything");
                        }
                        break;
                }
            }
            ctx.init(kms, tms, new SecureRandom());
            SSLSessionContext sessCtx;
            if (isServer) {
                sessCtx = ctx.getServerSessionContext();
            } else {
                sessCtx = ctx.getClientSessionContext();
            }
            if (sessionCacheSize > 0) {
                sessCtx.setSessionCacheSize((int) Math.min(sessionCacheSize, Integer.MAX_VALUE));
            }
            if (sessionTimeout > 0) {
                sessCtx.setSessionTimeout((int) Math.min(sessionTimeout, Integer.MAX_VALUE));
            }
            return ctx;
        } catch (Exception e) {
            if (e instanceof SSLException) {
                throw (SSLException) e;
            }
            throw new SSLException("failed to initialize the SSL context", e);
        }
    }

    public SslContextBuilder sessionCacheSize(long sessionCacheSize) {
        this.sessionCacheSize = sessionCacheSize;
        return this;
    }

    public SslContextBuilder sessionTimeout(long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
        return this;
    }

    private PrivateKey toPrivateKey(InputStream keyInputStream, String keyPassword) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException, KeyException, IOException, CertificateException {
        if (keyInputStream == null) {
            return null;
        }
        return getPrivateKeyFromByteBuffer(readCertificates(keyInputStream).get(0), keyPassword);
    }

    public X509Certificate[] toX509Certificates(InputStream in) throws CertificateException {
        Assert.notNull(in, "null inputstream");
        return getCertificatesFromBuffers(readCertificates(in));
    }

    public X509Certificate[] toX509Certificates(List<String> ls) throws CertificateException {
        Assert.notNull(ls, "null ls");
        return getCertificatesFromBuffers(readCertificates(ls));
    }

    public SslContextBuilder trust(boolean trustAll) {
        needClient();
        trustType = trustAll ? TrustType.ALL : trustType;
        return this;
    }

    public SslContextBuilder trust(InputStream trustCertCollectionInputStream) {
        needClient();
        try {
            return trust(toX509Certificates(trustCertCollectionInputStream));
        } catch (Exception e) {
            throw new IllegalArgumentException("Input stream does not contain valid certificate.", e);
        }
    }

    public SslContextBuilder trust(TrustManagerFactory trustManagerFactory) {
        needClient();
        Assert.notNull(trustManagerFactory, "null trustManagerFactory");
        this.trustManagerFactory = trustManagerFactory;
        this.trustType = TrustType.TrustManagerFactory;
        return this;
    }

    public SslContextBuilder trust(X509Certificate... trustCertCollection) {
        needClient();
        Assert.notEmpty(trustCertCollection, "empty trustCertCollection");
        if (rawX509Certificates == null) {
            rawX509Certificates = new ArrayList<>(trustCertCollection.length);
        }
        for (X509Certificate certificate : trustCertCollection) {
            if (certificate != null) {
                rawX509Certificates.add(certificate);
            }
        }
        trustType = TrustType.X509Certificate;
        return this;
    }

    public SslContextBuilder trust(X509TrustManager x509TrustManager) {
        needClient();
        Assert.notNull(x509TrustManager, "null x509TrustManager");
        this.x509TrustManager = x509TrustManager;
        this.trustType = TrustType.X509TrustManager;
        return this;
    }

    enum TrustType {

        ALL, TrustManagerFactory, X509TrustManager, X509Certificate

    }

    static class TrustAllX509TrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

    }


    static class TrustX509TrustManager implements X509TrustManager {

        final X509Certificate[] certificates;

        TrustX509TrustManager(X509Certificate[] certificates) {
            this.certificates = certificates;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] server_certs, String arg1) throws CertificateException {
            for (int i = 0; i < certificates.length; i++) {
                X509Certificate client_cert = certificates[i];
                for (int j = 0; j < server_certs.length; j++) {
                    if (client_cert.equals(server_certs[j])) {
                        return;
                    }
                }
            }
            throw new CertificateException();
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

    }

    static class TrustOneX509TrustManager implements X509TrustManager {

        X509Certificate certificate;

        TrustOneX509TrustManager(X509Certificate certificate) {
            this.certificate = certificate;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] server_certs, String arg1) throws CertificateException {
            X509Certificate client_cert = certificate;
            for (int i = 0; i < server_certs.length; i++) {
                if (client_cert.equals(server_certs[i])) {
                    return;
                }
            }
            throw new CertificateException();
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

    }

}
