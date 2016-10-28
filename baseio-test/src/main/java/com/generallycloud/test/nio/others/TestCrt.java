package com.generallycloud.test.nio.others;

import java.security.cert.CertificateException;

import com.generallycloud.nio.common.ssl.SelfSignedCertificate;

public class TestCrt {

	public static void main(String[] args) throws CertificateException {

		SelfSignedCertificate ssc = new SelfSignedCertificate();

		ssc.generate("D://TEMP/",2048);

	}
}
