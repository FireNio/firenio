package com.generallycloud.nio.common.ssl;

import java.io.File;
import java.io.IOException;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;

public class SSLUtil {

	private static SslContext	sslContext;

	private static Logger		logger	= LoggerFactory.getLogger(SSLUtil.class);

	public synchronized static SslContext initServer(File privateKey, File certificate) throws IOException {
		if (sslContext == null) {
			doInit(privateKey, certificate);
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

	private static void doInit(File privateKey, File certificate) throws IOException {

		LoggerUtil.prettyNIOServerLog(logger, "加载证书公钥：{}", certificate.getCanonicalPath());
		LoggerUtil.prettyNIOServerLog(logger, "加载证书私钥：{}", privateKey.getCanonicalPath());

		sslContext = SslContextBuilder.forServer(certificate, privateKey).build();

	}

	public static SSLEngine getSslEngine() {
		return sslContext.newEngine();
	}

	public static SslContext getSslContext() {
		return sslContext;
	}

}
