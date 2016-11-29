package com.generallycloud.test.nio.others;

import java.security.cert.CertificateException;

import com.generallycloud.nio.component.ssl.SelfSignedCertificate;

public class TestCrt {

	public static void main(String[] args) throws CertificateException {

		SelfSignedCertificate ssc = new SelfSignedCertificate("127.0.0.1");

		ssc.generate("D://TEMP/",2048);

	}
}
