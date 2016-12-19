package com.generallycloud.nio.component.ssl;

import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * A client-side {@link SslContext} which uses JDK's SSL/TLS implementation.
 *
 */
final class JdkSslClientContext extends JdkSslContext {

	JdkSslClientContext(X509Certificate[] trustCertCollection, TrustManagerFactory trustManagerFactory,
			X509Certificate[] keyCertChain, PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory,
			Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn,
			long sessionCacheSize, long sessionTimeout) throws SSLException {
		super(newSSLContext(trustCertCollection, trustManagerFactory, keyCertChain, key, keyPassword,
				keyManagerFactory, sessionCacheSize, sessionTimeout), true, ciphers, cipherFilter,
				toNegotiator(apn, false), ClientAuth.NONE, false);
	}

	private static SSLContext newSSLContext(X509Certificate[] trustCertCollection,
			TrustManagerFactory trustManagerFactory, X509Certificate[] keyCertChain, PrivateKey key,
			String keyPassword, KeyManagerFactory keyManagerFactory, long sessionCacheSize, long sessionTimeout)
			throws SSLException {
		try {
			if (trustCertCollection != null) {
				trustManagerFactory = buildTrustManagerFactory(trustCertCollection, trustManagerFactory);
			}
			if (keyCertChain != null) {
				keyManagerFactory = buildKeyManagerFactory(keyCertChain, key, keyPassword, keyManagerFactory);
			}
			SSLContext ctx = SSLContext.getInstance(PROTOCOL);

			TrustManager[] tms;
			KeyManager[] kms = null;

			if (keyManagerFactory == null) {

				X509TrustManager x509m = new X509TrustManager() {

					@Override
					public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
							throws java.security.cert.CertificateException {

					}

					@Override
					public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
							throws java.security.cert.CertificateException {

					}

					@Override
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return null;
					}
				};

				tms = new X509TrustManager[] { x509m };
			} else {

				tms = trustManagerFactory.getTrustManagers();
			}

			if (keyManagerFactory != null) {
				kms = keyManagerFactory.getKeyManagers();
			}

			ctx.init(kms, tms, new SecureRandom());

			SSLSessionContext sessCtx = ctx.getClientSessionContext();
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
			throw new SSLException("failed to initialize the client-side SSL context", e);
		}
	}
}
