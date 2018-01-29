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
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyException;
import java.security.KeyFactory;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;

import com.generallycloud.baseio.common.Assert;
import com.generallycloud.baseio.component.ByteArrayInputStream;

/**
 * Builder for configuring a new SslContext for creation.
 */
public final class SslContextBuilder {

    private KeyManagerFactory buildDefaultKeyManagerFactory()
            throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory
                .getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(null, null);
        return keyManagerFactory;
    }

    public static SslProvider defaultProvider() {
        if (OpenSsl.isAvailable()) {
            return SslProvider.OPENSSL;
        } else {
            return SslProvider.JDK;
        }
    }

    public static SslContextBuilder forClient() {
        return new SslContextBuilder(false);
    }

    public static SslContextBuilder forServer(File keyCertChainFile, File keyFile) {
        return new SslContextBuilder(true).keyManager(keyCertChainFile, keyFile);
    }

    public static SslContextBuilder forServer(File keyCertChainFile, File keyFile,
            String keyPassword) {
        return new SslContextBuilder(true).keyManager(keyCertChainFile, keyFile, keyPassword);
    }

    public static SslContextBuilder forServer(InputStream keyCertChainInputStream,
            InputStream keyInputStream) {
        return new SslContextBuilder(true).keyManager(keyCertChainInputStream, keyInputStream);
    }

    public static SslContextBuilder forServer(InputStream keyCertChainInputStream,
            InputStream keyInputStream, String keyPassword) {
        return new SslContextBuilder(true).keyManager(keyCertChainInputStream, keyInputStream,
                keyPassword);
    }

    public static SslContextBuilder forServer(KeyManagerFactory keyManagerFactory) {
        return new SslContextBuilder(true).keyManager(keyManagerFactory);
    }

    public static SslContextBuilder forServer(PrivateKey key, String keyPassword,
            X509Certificate... keyCertChain) {
        return new SslContextBuilder(true).keyManager(key, keyPassword, keyCertChain);
    }

    public static SslContextBuilder forServer(PrivateKey key, X509Certificate... keyCertChain) {
        return new SslContextBuilder(true).keyManager(key, keyCertChain);
    }

    private PKCS8EncodedKeySpec generateKeySpec(String password, byte[] key)
            throws IOException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeySpecException, InvalidKeyException, InvalidAlgorithmParameterException {
        if (password == null) {
            return new PKCS8EncodedKeySpec(key);
        }
        EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = new EncryptedPrivateKeyInfo(key);
        SecretKeyFactory keyFactory = SecretKeyFactory
                .getInstance(encryptedPrivateKeyInfo.getAlgName());
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
        SecretKey pbeKey = keyFactory.generateSecret(pbeKeySpec);

        Cipher cipher = Cipher.getInstance(encryptedPrivateKeyInfo.getAlgName());
        cipher.init(Cipher.DECRYPT_MODE, pbeKey, encryptedPrivateKeyInfo.getAlgParameters());

        return encryptedPrivateKeyInfo.getKeySpec(cipher);
    }

    private X509Certificate[] getCertificatesFromBuffers(byte[][] certs)
            throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate[] x509Certs = new X509Certificate[certs.length];

        for (int i = 0; i < certs.length; i++) {
            x509Certs[i] = (X509Certificate) cf
                    .generateCertificate(new ByteArrayInputStream(certs[i]));
        }
        return x509Certs;
    }

    private PrivateKey getPrivateKeyFromByteBuffer(byte[] encodedKey, String keyPassword)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException,
            InvalidAlgorithmParameterException, KeyException, IOException {

        PKCS8EncodedKeySpec encodedKeySpec = generateKeySpec(keyPassword, encodedKey);
        try {
            return KeyFactory.getInstance("RSA").generatePrivate(encodedKeySpec);
        } catch (InvalidKeySpecException ignore) {}
        try {
            return KeyFactory.getInstance("DSA").generatePrivate(encodedKeySpec);
        } catch (InvalidKeySpecException ignore2) {}
        try {
            return KeyFactory.getInstance("EC").generatePrivate(encodedKeySpec);
        } catch (InvalidKeySpecException e) {
            throw new InvalidKeySpecException("Neither RSA, DSA nor EC worked", e);
        }
    }

    private SslContext newClientContextInternal(SslProvider provider, X509Certificate[] trustCert,
            TrustManagerFactory trustManagerFactory, X509Certificate[] keyCertChain, PrivateKey key,
            String keyPassword, KeyManagerFactory keyManagerFactory, List<String> ciphers,
            ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout)
            throws SSLException {
        if (provider == null) {
            provider = defaultProvider();
        }
        switch (provider) {
            case JDK:
                return new JdkSslClientContext(trustCert, trustManagerFactory, keyCertChain, key,
                        keyPassword, keyManagerFactory, ciphers, apn, sessionCacheSize,
                        sessionTimeout);
            case OPENSSL:
                return null;
            default:
                throw new Error(provider.toString());
        }
    }

    private SslContext newServerContextInternal(SslProvider provider,
            X509Certificate[] trustCertCollection, TrustManagerFactory trustManagerFactory,
            X509Certificate[] keyCertChain, PrivateKey key, String keyPassword,
            KeyManagerFactory keyManagerFactory, List<String> ciphers,
            ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout,
            ClientAuth clientAuth) throws SSLException {

        if (provider == null) {
            provider = defaultProvider();
        }

        switch (provider) {
            case JDK:
                return new JdkSslServerContext(trustCertCollection, trustManagerFactory,
                        keyCertChain, key, keyPassword, keyManagerFactory, ciphers, apn,
                        sessionCacheSize, sessionTimeout, clientAuth);
            case OPENSSL:
                return null;
            default:
                throw new Error(provider.toString());
        }
    }

    private PrivateKey toPrivateKey(File keyFile, String keyPassword)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException,
            InvalidAlgorithmParameterException, KeyException, IOException {
        if (keyFile == null) {
            return null;
        }
        return getPrivateKeyFromByteBuffer(PemReader.readPrivateKey(keyFile), keyPassword);
    }

    private PrivateKey toPrivateKey(InputStream keyInputStream, String keyPassword)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException,
            InvalidAlgorithmParameterException, KeyException, IOException {
        if (keyInputStream == null) {
            return null;
        }
        return getPrivateKeyFromByteBuffer(PemReader.readPrivateKey(keyInputStream), keyPassword);
    }

    private PrivateKey toPrivateKeyInternal(File keyFile, String keyPassword) throws SSLException {
        try {
            return toPrivateKey(keyFile, keyPassword);
        } catch (Exception e) {
            throw new SSLException(e);
        }
    }

    private X509Certificate[] toX509Certificates(File file) throws CertificateException {
        if (file == null) {
            return null;
        }
        return getCertificatesFromBuffers(PemReader.readCertificates(file));
    }

    private X509Certificate[] toX509Certificates(InputStream in) throws CertificateException {
        if (in == null) {
            return null;
        }
        return getCertificatesFromBuffers(PemReader.readCertificates(in));
    }

    private X509Certificate[] toX509CertificatesInternal(File file) throws SSLException {
        try {
            return toX509Certificates(file);
        } catch (CertificateException e) {
            throw new SSLException(e);
        }
    }

    private ApplicationProtocolConfig apn;
    private List<String>              ciphers;
    private ClientAuth                clientAuth = ClientAuth.NONE;
    private final boolean             forServer;
    private PrivateKey                key;
    private X509Certificate[]         keyCertChain;
    private KeyManagerFactory         keyManagerFactory;
    private String                    keyPassword;
    private SslProvider               provider;
    private long                      sessionCacheSize;
    private long                      sessionTimeout;
    private X509Certificate[]         trustCertCollection;
    private TrustManagerFactory       trustManagerFactory;

    private SslContextBuilder(boolean forServer) {
        this.forServer = forServer;
    }

    public SslContextBuilder applicationProtocolConfig(ApplicationProtocolConfig apn) {
        this.apn = apn;
        return this;
    }

    public SslContext build() throws SSLException {
        if (forServer) {
            return newServerContextInternal(provider, trustCertCollection, trustManagerFactory,
                    keyCertChain, key, keyPassword, keyManagerFactory, ciphers, apn,
                    sessionCacheSize, sessionTimeout, clientAuth);
        } else {
            return newClientContextInternal(provider, trustCertCollection, trustManagerFactory,
                    keyCertChain, key, keyPassword, keyManagerFactory, ciphers, apn,
                    sessionCacheSize, sessionTimeout);
        }
    }

    public SslContextBuilder ciphers(List<String> ciphers) {
        this.ciphers = ciphers;
        return this;
    }

    public SslContextBuilder clientAuth(ClientAuth clientAuth) {
        this.clientAuth = clientAuth;
        return this;
    }

    public SslContextBuilder keyManager(File keyCertChainFile, File keyFile) {
        return keyManager(keyCertChainFile, keyFile, null);
    }

    public SslContextBuilder keyManager(File keyCertChainFile, File keyFile, String keyPassword) {
        X509Certificate[] keyCertChain;
        PrivateKey key;
        try {
            keyCertChain = toX509Certificates(keyCertChainFile);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "File does not contain valid certificates: " + keyCertChainFile, e);
        }
        try {
            key = toPrivateKey(keyFile, keyPassword);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "File does not contain valid private key: " + keyFile, e);
        }
        return keyManager(key, keyPassword, keyCertChain);
    }

    public SslContextBuilder keyManager(InputStream keyCertChainInputStream,
            InputStream keyInputStream) {
        return keyManager(keyCertChainInputStream, keyInputStream, null);
    }

    public SslContextBuilder keyManager(InputStream keyCertChainInputStream,
            InputStream keyInputStream, String keyPassword) {
        X509Certificate[] keyCertChain;
        PrivateKey key;
        try {
            keyCertChain = toX509Certificates(keyCertChainInputStream);
        } catch (Exception e) {
            throw new IllegalArgumentException("Input stream not contain valid certificates.", e);
        }
        try {
            key = toPrivateKey(keyInputStream, keyPassword);
        } catch (Exception e) {
            throw new IllegalArgumentException("Input stream does not contain valid private key.",
                    e);
        }
        return keyManager(key, keyPassword, keyCertChain);
    }

    public SslContextBuilder keyManager(KeyManagerFactory keyManagerFactory) {
        if (forServer) {
            Assert.notNull(keyManagerFactory, "keyManagerFactory required for servers");
        }
        keyCertChain = null;
        key = null;
        keyPassword = null;
        this.keyManagerFactory = keyManagerFactory;
        return this;
    }

    public SslContextBuilder keyManager(PrivateKey key, String keyPassword,
            X509Certificate... keyCertChain) {
        if (forServer) {
            if (keyCertChain.length == 0) {
                throw new IllegalArgumentException("keyCertChain must be non-empty");
            }
        }
        if (keyCertChain == null || keyCertChain.length == 0) {
            this.keyCertChain = null;
        } else {
            for (X509Certificate cert : keyCertChain) {
                if (cert == null) {
                    throw new IllegalArgumentException("keyCertChain contains null entry");
                }
            }
            this.keyCertChain = keyCertChain.clone();
        }
        this.key = key;
        this.keyPassword = keyPassword;
        this.keyManagerFactory = null;
        return this;
    }

    public SslContextBuilder keyManager(PrivateKey key, X509Certificate... keyCertChain) {
        return keyManager(key, null, keyCertChain);
    }

    public SslContextBuilder sessionCacheSize(long sessionCacheSize) {
        this.sessionCacheSize = sessionCacheSize;
        return this;
    }

    public SslContextBuilder sessionTimeout(long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
        return this;
    }

    public SslContextBuilder sslProvider(SslProvider provider) {
        this.provider = provider;
        return this;
    }

    public SslContextBuilder trustManager(File trustCertCollectionFile) {
        try {
            return trustManager(toX509Certificates(trustCertCollectionFile));
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "File does not contain valid certificates: " + trustCertCollectionFile, e);
        }
    }

    public SslContextBuilder trustManager(InputStream trustCertCollectionInputStream) {
        try {
            return trustManager(toX509Certificates(trustCertCollectionInputStream));
        } catch (Exception e) {
            throw new IllegalArgumentException("Input stream does not contain valid certificates.",
                    e);
        }
    }

    public SslContextBuilder trustManager(TrustManagerFactory trustManagerFactory) {
        trustCertCollection = null;
        this.trustManagerFactory = trustManagerFactory;
        return this;
    }

    public SslContextBuilder trustManager(X509Certificate... trustCertCollection) {
        this.trustCertCollection = trustCertCollection != null ? trustCertCollection.clone() : null;
        trustManagerFactory = null;
        return this;
    }

}
