package com.generallycloud.nio.component.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import com.generallycloud.nio.Encoding;
import com.generallycloud.nio.common.BASE64Util;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.FileUtil;

final class PemReader {

	static byte[][] readCertificates(File file) throws CertificateException {
		try {
			InputStream in = new FileInputStream(file);

			try {
				return readCertificates(in);
			} finally {
				CloseUtil.close(in);
			}
		} catch (FileNotFoundException e) {
			throw new CertificateException("could not find certificate file: " + file);
		}
	}

	static byte[][] readCertificates(InputStream in) throws CertificateException {
		String content;
		try {
			content = FileUtil.input2String(in, Encoding.UTF8);
		} catch (IOException e) {
			throw new CertificateException("failed to read certificate input stream", e);
		}

		String[] ls = content.split("\n");

		StringBuilder b = new StringBuilder();

		for (String s : ls) {
			if (s.startsWith("----")) {
				continue;
			}
			b.append(s.trim().replace("\r", ""));
		}

		List<byte[]> certs = new ArrayList<byte[]>();

		byte[] data = BASE64Util.base64ToByteArray(b.toString());

		certs.add(data);

		if (certs.isEmpty()) {
			throw new CertificateException("found no certificates in input stream");
		}

		return certs.toArray(new byte[][] {});
	}

	static byte[] readPrivateKey(File file) throws KeyException {
		try {
			InputStream in = new FileInputStream(file);

			try {
				return readPrivateKey(in);
			} finally {
				CloseUtil.close(in);
			}
		} catch (FileNotFoundException e) {
			throw new KeyException("could not fine key file: " + file);
		}
	}

	static byte[] readPrivateKey(InputStream in) throws KeyException {
		String content;
		try {
			content = FileUtil.input2String(in, Encoding.UTF8);
		} catch (IOException e) {
			throw new KeyException("failed to read key input stream", e);
		}

		String[] ls = content.split("\n");

		StringBuilder b = new StringBuilder();

		for (String s : ls) {
			if (s.startsWith("-----")) {
				continue;
			}
			b.append(s.trim().replace("\r", ""));
		}

		byte[] der = BASE64Util.base64ToByteArray(b.toString());
		return der;
	}

	private PemReader() {
	}
}
