package com.generallycloud.nio.common.ssl;

import java.io.File;

import javax.net.ssl.SSLEngine;

import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.common.StringUtil;

public class SSLUtil {

	static SslContext	sslContext;

	public synchronized static SslContext initServer(String base) {
		if (sslContext == null) {
			doInit(base);
		}
		return sslContext;
	}

	public synchronized static SslContext initClient() {
		if (sslContext == null) {
			doInit(null);
		}
		return sslContext;
	}

	private static void doInit(String base) {

		try {

			if (StringUtil.isNullOrBlank(base)) {

				sslContext = SslContextBuilder.forClient().build();

			} else {

				// SelfSignedCertificate ssc = new SelfSignedCertificate();
				// sslContext =
				// SslContextBuilder.forServer(ssc.certificate(),
				// ssc.privateKey()).build();

				File certificate = SharedBundle.instance().loadFile(base + "/conf/generallycloud.com.crt");
				File privateKey = SharedBundle.instance().loadFile(base + "/conf/generallycloud.com.key");

				// File certificate =
				// SharedBundle.instance().loadFile("http/conf/keyutil_example.com1.crt");
				// File privateKey =
				// SharedBundle.instance().loadFile("http/conf/keyutil_example.com1.key");
				//
				sslContext = SslContextBuilder.forServer(certificate, privateKey).build();

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static SSLEngine getSslEngine() {
		return sslContext.newEngine();
	}

}
