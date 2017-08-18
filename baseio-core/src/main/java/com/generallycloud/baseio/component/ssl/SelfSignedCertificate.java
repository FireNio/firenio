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
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
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

import com.generallycloud.baseio.common.BASE64Util;
import com.generallycloud.baseio.common.Encoding;
import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

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

@SuppressWarnings("restriction")
public final class SelfSignedCertificate {

    /** The maximum possible value in X.509 specification: 9999-12-31 23:59:59 */
    private static final Date   DEFAULT_NOT_AFTER  = new Date(253402300799000L);

    /**
     * Current time minus 1 year, just in case software clock goes back due to
     * time synchronization
     */
    private static final Date   DEFAULT_NOT_BEFORE = new Date(
            System.currentTimeMillis() - 86400000L * 365);
    private static final Logger logger             = LoggerFactory
            .getLogger(SelfSignedCertificate.class);

    private X509Certificate     cert;
    private File                certificate;
    private String              fqdn;
    private PrivateKey          key;
    private Date                notAfter;
    private Date                notBefore;
    private File                privateKey;
    private SecureRandom        random;

    public SelfSignedCertificate() {
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
    public SelfSignedCertificate(Date notBefore, Date notAfter) {
        this("example.com", notBefore, notAfter);
    }

    /**
     * Creates a new instance.
     *
     * @param fqdn
     *             a fully qualified domain name
     */
    public SelfSignedCertificate(String fqdn) {
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
    public SelfSignedCertificate(String fqdn, Date notBefore, Date notAfter) {
        // Bypass entrophy collection by using insecure random generator.
        // We just want to generate it without any delay because it's for
        // testing purposes only.
        this(fqdn, new SecureRandom(), notBefore, notAfter);
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
    public SelfSignedCertificate(String fqdn, SecureRandom random) {
        this(fqdn, random, DEFAULT_NOT_BEFORE, DEFAULT_NOT_AFTER);
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
    public SelfSignedCertificate(String fqdn, SecureRandom random, Date notBefore, Date notAfter) {
        // Generate an RSA key pair.
        this.random = random;
        this.fqdn = fqdn;
        this.notBefore = notBefore;
        this.notAfter = notAfter;
    }

    /**
     * Returns the generated X.509 certificate.
     */
    public X509Certificate cert() {
        return cert;
    }

    /**
     * Returns the generated X.509 certificate file in PEM format.
     */
    public File certificate() {
        return certificate;
    }

    public void generate() throws CertificateEncodingException {
        this.generate(null, 1024);
    }

    public void generate(String fileRoot) throws CertificateEncodingException {
        this.generate(fileRoot, 1024);
    }

    public void generate(String fileRoot, int bits) throws CertificateEncodingException {

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

        File[] files;
        try {
            // Try the OpenJDK's proprietary implementation.
            files = generate(fileRoot, fqdn, keypair, random, notBefore, notAfter);
        } catch (Exception t) {

            logger.debug(
                    "Failed to generate a self-signed X.509 certificate using sun.security.x509:",
                    t);

            throw new Error(t);
        }

        certificate = files[0];
        privateKey = files[1];
        key = keypair.getPrivate();
        FileInputStream certificateInput = null;
        try {
            certificateInput = new FileInputStream(certificate);
            cert = (X509Certificate) CertificateFactory.getInstance("X509")
                    .generateCertificate(certificateInput);
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

    private File[] generate(String fileRoot, String fqdn, KeyPair keypair, SecureRandom random,
            Date notBefore, Date notAfter) throws Exception {
        PrivateKey key = keypair.getPrivate();

        // Prepare the information required for generating an X.509
        // certificate.
        X509CertInfo info = new X509CertInfo();
        X500Name owner = new X500Name("CN=" + fqdn);
        info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
        info.set(X509CertInfo.SERIAL_NUMBER,
                new CertificateSerialNumber(new BigInteger(64, random)));
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
        info.set(X509CertInfo.ALGORITHM_ID,
                new CertificateAlgorithmId(new AlgorithmId(AlgorithmId.sha1WithRSAEncryption_oid)));

        // Sign the cert to identify the algorithm that's used.
        X509CertImpl cert = new X509CertImpl(info);
        cert.sign(key, "SHA1withRSA");

        // Update the algorithm and sign again.
        info.set(CertificateAlgorithmId.NAME + '.' + CertificateAlgorithmId.ALGORITHM,
                cert.get(X509CertImpl.SIG_ALG));
        cert = new X509CertImpl(info);
        cert.sign(key, "SHA1withRSA");
        cert.verify(keypair.getPublic());

        return newSelfSignedCertificate(fileRoot, fqdn, key, cert);
    }

    /**
     * Returns the generated RSA private key.
     */
    public PrivateKey key() {
        return key;
    }

    protected File[] newSelfSignedCertificate(String fileRoot, String fqdn, PrivateKey key,
            X509Certificate cert) throws IOException, CertificateEncodingException {
        // Encode the private key into a file.
        byte keyArray[] = key.getEncoded();

        String keyText = BASE64Util.byteArrayToBase64(keyArray);

        keyText = "-----BEGIN PRIVATE KEY-----\n" + keyText + "\n-----END PRIVATE KEY-----\n";

        File keyFile = write2file(fileRoot, "keyutil_" + fqdn, ".key", keyText, Encoding.UTF8);

        byte certArray[] = cert.getEncoded();

        String certText = BASE64Util.byteArrayToBase64(certArray);

        certText = "-----BEGIN CERTIFICATE-----\n" + certText + "\n-----END CERTIFICATE-----\n";

        File certFile = write2file(fileRoot, "keyutil_" + fqdn, ".crt", certText, Encoding.UTF8);

        return new File[] { certFile, keyFile };
    }

    /**
     * Returns the generated RSA private key file in PEM format.
     */
    public File privateKey() {
        return privateKey;
    }

    private void safeDelete(File file) {
        if (!file.exists()) {
            return;
        }
        if (!file.delete()) {
            logger.error("Failed to delete a file: " + file);
        }
    }

    private File write2file(String fileRoot, String name, String subfix, String text,
            Charset charset) throws IOException {
        File file;
        if (StringUtil.isNullOrBlank(fileRoot)) {
            file = File.createTempFile(name + "_", subfix);
        } else {
            file = new File(fileRoot + File.separator + name + subfix);
        }

        safeDelete(file);

        FileUtil.writeByFile(file, text.getBytes(charset));

        logger.info("file generated:{}", file.getCanonicalPath());
        return file;
    }
}
