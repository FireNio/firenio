package com.generallycloud.nio.common.ssl;

import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateIssuerName;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateSubjectName;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

import java.util.Date;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;

@SuppressWarnings("restriction")
final class OpenJdkSelfSignedCertGenerator {

	static String[] generate(String fqdn, KeyPair keypair, SecureRandom random, Date notBefore, Date notAfter)
			throws Exception {
		PrivateKey key = keypair.getPrivate();

		// Prepare the information required for generating an X.509
		// certificate.
		X509CertInfo info = new X509CertInfo();
		X500Name owner = new X500Name("CN=" + fqdn);
		info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
		info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(new BigInteger(64, random)));
		try {
			info.set(X509CertInfo.SUBJECT, new CertificateSubjectName(owner));
		} catch (CertificateException ignore) {
			info.set(X509CertInfo.SUBJECT, owner);
		}
		try {
			info.set(X509CertInfo.ISSUER, new CertificateIssuerName(owner));
		} catch (CertificateException ignore) {
			info.set(X509CertInfo.ISSUER, owner);
		}
		info.set(X509CertInfo.VALIDITY, new CertificateValidity(notBefore, notAfter));
		info.set(X509CertInfo.KEY, new CertificateX509Key(keypair.getPublic()));
		info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(new AlgorithmId(
				AlgorithmId.sha1WithRSAEncryption_oid)));

		// Sign the cert to identify the algorithm that's used.
		X509CertImpl cert = new X509CertImpl(info);
		cert.sign(key, "SHA1withRSA");

		// Update the algorithm and sign again.
		info.set(CertificateAlgorithmId.NAME + '.' + CertificateAlgorithmId.ALGORITHM, cert.get(X509CertImpl.SIG_ALG));
		cert = new X509CertImpl(info);
		cert.sign(key, "SHA1withRSA");
		cert.verify(keypair.getPublic());

		return SelfSignedCertificate.newSelfSignedCertificate(fqdn, key, cert);
	}

	private OpenJdkSelfSignedCertGenerator() {
	}
}
