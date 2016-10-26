package com.generallycloud.nio.common.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

import com.generallycloud.nio.Encoding;
import com.generallycloud.nio.common.BASE64Util;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;

public final class SelfSignedCertificate {

	private static final Logger	logger			= LoggerFactory.getLogger(SelfSignedCertificate.class);

	/**
	 * Current time minus 1 year, just in case software clock goes back due to
	 * time synchronization
	 */
	private static final Date	DEFAULT_NOT_BEFORE	= new Date(System.currentTimeMillis() - 86400000L * 365);
	/** The maximum possible value in X.509 specification: 9999-12-31 23:59:59 */
	private static final Date	DEFAULT_NOT_AFTER	= new Date(253402300799000L);

	private final File			certificate;
	private final File			privateKey;
	private final X509Certificate	cert;
	private final PrivateKey		key;

	/**
	 * Creates a new instance.
	 */
	public SelfSignedCertificate() throws CertificateException {
		this(DEFAULT_NOT_BEFORE, DEFAULT_NOT_AFTER);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param notBefore
	 *             Certificate is not valid before this time
	 * @param notAfter
	 *             Certificate is not valid after this time
	 */
	public SelfSignedCertificate(Date notBefore, Date notAfter) throws CertificateException {
		this("example.com", notBefore, notAfter);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param fqdn
	 *             a fully qualified domain name
	 */
	public SelfSignedCertificate(String fqdn) throws CertificateException {
		this(fqdn, DEFAULT_NOT_BEFORE, DEFAULT_NOT_AFTER);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param fqdn
	 *             a fully qualified domain name
	 * @param notBefore
	 *             Certificate is not valid before this time
	 * @param notAfter
	 *             Certificate is not valid after this time
	 */
	public SelfSignedCertificate(String fqdn, Date notBefore, Date notAfter) throws CertificateException {
		// Bypass entrophy collection by using insecure random generator.
		// We just want to generate it without any delay because it's for
		// testing purposes only.
		this(fqdn, new SecureRandom(), 1024, notBefore, notAfter);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param fqdn
	 *             a fully qualified domain name
	 * @param random
	 *             the {@link java.security.SecureRandom} to use
	 * @param bits
	 *             the number of bits of the generated private key
	 */
	public SelfSignedCertificate(String fqdn, SecureRandom random, int bits) throws CertificateException {
		this(fqdn, random, bits, DEFAULT_NOT_BEFORE, DEFAULT_NOT_AFTER);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param fqdn
	 *             a fully qualified domain name
	 * @param random
	 *             the {@link java.security.SecureRandom} to use
	 * @param bits
	 *             the number of bits of the generated private key
	 * @param notBefore
	 *             Certificate is not valid before this time
	 * @param notAfter
	 *             Certificate is not valid after this time
	 */
	public SelfSignedCertificate(String fqdn, SecureRandom random, int bits, Date notBefore, Date notAfter)
			throws CertificateException {
		// Generate an RSA key pair.
		final KeyPair keypair;
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(bits, random);
			keypair = keyGen.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			// Should not reach here because every Java implementation must
			// have RSA key pair generator.
			throw new Error(e);
		}

		String[] paths;
		try {
			// Try the OpenJDK's proprietary implementation.
			paths = OpenJdkSelfSignedCertGenerator.generate(fqdn, keypair, random, notBefore, notAfter);
		} catch (Exception t) {

			logger.debug("Failed to generate a self-signed X.509 certificate using sun.security.x509:", t);

			throw new Error(t);
		}

		certificate = new File(paths[0]);
		privateKey = new File(paths[1]);
		key = keypair.getPrivate();
		FileInputStream certificateInput = null;
		try {
			certificateInput = new FileInputStream(certificate);
			cert = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(certificateInput);
		} catch (Exception e) {
			throw new CertificateEncodingException(e);
		} finally {
			if (certificateInput != null) {
				try {
					certificateInput.close();
				} catch (IOException e) {
					logger.error("Failed to close a file: " + certificate, e);
				}
			}
		}
	}

	/**
	 * Returns the generated X.509 certificate file in PEM format.
	 */
	public File certificate() {
		return certificate;
	}

	/**
	 * Returns the generated RSA private key file in PEM format.
	 */
	public File privateKey() {
		return privateKey;
	}

	/**
	 * Returns the generated X.509 certificate.
	 */
	public X509Certificate cert() {
		return cert;
	}

	/**
	 * Returns the generated RSA private key.
	 */
	public PrivateKey key() {
		return key;
	}

	/**
	 * Deletes the generated X.509 certificate file and RSA private key file.
	 */
	public void delete() {
		safeDelete(certificate);
		safeDelete(privateKey);
	}

	static String[] newSelfSignedCertificate(String fqdn, PrivateKey key, X509Certificate cert) throws IOException,
			CertificateEncodingException {
		// Encode the private key into a file.
		byte keyArray[] = key.getEncoded();

		String keyText = BASE64Util.byteArrayToBase64(keyArray);

		keyText = "-----BEGIN PRIVATE KEY-----\n" + keyText + "\n-----END PRIVATE KEY-----\n";

		File keyFile = File.createTempFile("keyutil_" + fqdn + '_', ".key");
		keyFile.deleteOnExit();

		OutputStream keyOut = new FileOutputStream(keyFile);
		try {
			keyOut.write(keyText.getBytes(Encoding.UTF8));
			keyOut.close();
			keyOut = null;
		} finally {
			if (keyOut != null) {
				safeClose(keyFile, keyOut);
				safeDelete(keyFile);
			}
		}

		byte certArray[] = cert.getEncoded();

		String certText = BASE64Util.byteArrayToBase64(certArray);

		certText = "-----BEGIN CERTIFICATE-----\n" + certText + "\n-----END CERTIFICATE-----\n";

		File certFile = File.createTempFile("keyutil_" + fqdn + '_', ".crt");

		certFile.deleteOnExit();

		OutputStream certOut = new FileOutputStream(certFile);
		try {
			certOut.write(certText.getBytes(Encoding.UTF8));
			certOut.close();
			certOut = null;
		} finally {
			if (certOut != null) {
				safeClose(certFile, certOut);
				safeDelete(certFile);
				safeDelete(keyFile);
			}
		}

		return new String[] { certFile.getPath(), keyFile.getPath() };
	}

	private static void safeDelete(File certFile) {
		if (!certFile.delete()) {
			logger.error("Failed to delete a file: " + certFile);
		}
	}

	private static void safeClose(File keyFile, OutputStream keyOut) {
		try {
			keyOut.close();
		} catch (IOException e) {
			logger.error("Failed to close a file: " + keyFile, e);
		}
	}
}
