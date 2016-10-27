package com.generallycloud.nio.common.ssl;

import java.io.File;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.common.SharedBundle;

public class SSLUtil {

	private static SslContext	sslContext;

	private static Logger		logger	= LoggerFactory.getLogger(SSLUtil.class);

	public synchronized static SslContext initServer(String base) {
		if (sslContext == null) {
			doInit(base);
		}
		return sslContext;
	}

	public synchronized static SslContext initClient() {
		if (sslContext == null) {
			try {
				sslContext = SslContextBuilder.forClient().build();
			} catch (SSLException e) {
				e.printStackTrace();
			}
		}
		return sslContext;
	}

	private static void doInit(String base) {

		try {
			// SelfSignedCertificate ssc = new SelfSignedCertificate();
			// sslContext =
			// SslContextBuilder.forServer(ssc.certificate(),
			// ssc.privateKey()).build();

			File certificate = SharedBundle.instance().loadFile(base + "/conf/generallycloud.com.crt");
			File privateKey = SharedBundle.instance().loadFile(base + "/conf/generallycloud.com.key");

			LoggerUtil.prettyNIOServerLog(logger, "加载证书公钥：{}", certificate.getCanonicalPath());
			LoggerUtil.prettyNIOServerLog(logger, "加载证书私钥：{}", privateKey.getCanonicalPath());

			// File certificate =
			// SharedBundle.instance().loadFile("http/conf/keyutil_example.com1.crt");
			// File privateKey =
			// SharedBundle.instance().loadFile("http/conf/keyutil_example.com1.key");
			//
			sslContext = SslContextBuilder.forServer(certificate, privateKey).build();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static SSLEngine getSslEngine() {
		return sslContext.newEngine();
	}

}
