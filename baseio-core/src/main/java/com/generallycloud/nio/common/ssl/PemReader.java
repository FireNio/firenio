package com.generallycloud.nio.common.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.generallycloud.nio.Encoding;
import com.generallycloud.nio.common.BASE64Util;
import com.generallycloud.nio.common.FileUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;

/**
 * Reads a PEM file and converts it into a list of DERs so that they are
 * imported into a {@link KeyStore} easily.
 */
final class PemReader {

	private static final Logger	logger		= LoggerFactory.getLogger(PemReader.class);

	private static final Pattern	CERT_PATTERN	= Pattern.compile("-+BEGIN\\s+.*CERTIFICATE[^-]*-+(?:\\s|\\r|\\n)+" + // Header
												"([a-z0-9+/=\\r\\n]+)" + // Base64
																	// text
												"-+END\\s+.*CERTIFICATE[^-]*-+", // Footer
												Pattern.CASE_INSENSITIVE);
	private static final Pattern	KEY_PATTERN	= Pattern.compile(
												"-+BEGIN\\s+.*PRIVATE\\s+KEY[^-]*-+(?:\\s|\\r|\\n)+" + // Header
														"([a-z0-9+/=\\r\\n]+)" + // Base64
																			// text
														"-+END\\s+.*PRIVATE\\s+KEY[^-]*-+", // Footer
												Pattern.CASE_INSENSITIVE);

	static byte[][] readCertificates(File file) throws CertificateException {
		try {
			InputStream in = new FileInputStream(file);

			try {
				return readCertificates(in);
			} finally {
				safeClose(in);
			}
		} catch (FileNotFoundException e) {
			throw new CertificateException("could not find certificate file: " + file);
		}
	}

	static byte[][] readCertificates(InputStream in) throws CertificateException {
		String content;
		try {
			content = readContent(in);
		} catch (IOException e) {
			throw new CertificateException("failed to read certificate input stream", e);
		}

		List<byte[]> certs = new ArrayList<byte[]>();
		Matcher m = CERT_PATTERN.matcher(content);
		int start = 0;
		for (;;) {
			if (!m.find(start)) {
				break;
			}

			String c = m.group(1);
			
			c = c.replace("\n", "");

			byte[] data = BASE64Util.base64ToByteArray(c);

			certs.add(data);

			start = m.end();
		}

		if (certs.isEmpty()) {
			throw new CertificateException("found no certificates in input stream");
		}

		return certs.toArray(new byte[][]{});
	}

	static byte[] readPrivateKey(File file) throws KeyException {
		try {
			InputStream in = new FileInputStream(file);

			try {
				return readPrivateKey(in);
			} finally {
				safeClose(in);
			}
		} catch (FileNotFoundException e) {
			throw new KeyException("could not fine key file: " + file);
		}
	}

	static byte[] readPrivateKey(InputStream in) throws KeyException {
		String content;
		try {
			content = readContent(in);
		} catch (IOException e) {
			throw new KeyException("failed to read key input stream", e);
		}

//		Matcher m = KEY_PATTERN.matcher(content);
//		if (!m.find()) {
//			throw new KeyException("could not find a PKCS #8 private key in input stream");
//		}

		String [] ls = content.split("\n");
		
		StringBuilder b = new StringBuilder();
		
		for(String s : ls){
			if (s.startsWith("-----")) {
				continue;
			}
			b.append(s);
		}
		
		byte[] der = BASE64Util.base64ToByteArray(b.toString());
		return der;
	}

	private static String readContent(InputStream in) throws IOException {
		return FileUtil.input2String(in, Encoding.UTF8);
	}

	private static void safeClose(InputStream in) {
		try {
			in.close();
		} catch (IOException e) {
			logger.error("Failed to close a stream.", e);
		}
	}

	private PemReader() {
	}
}
