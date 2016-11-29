package com.generallycloud.nio.component.ssl;

import java.io.File;
import java.io.IOException;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.component.ssl.ApplicationProtocolConfig.Protocol;
import com.generallycloud.nio.component.ssl.ApplicationProtocolConfig.SelectedListenerFailureBehavior;
import com.generallycloud.nio.component.ssl.ApplicationProtocolConfig.SelectorFailureBehavior;

public class SSLUtil {

	private static SslContext	sslContext;

	private static Logger		logger	= LoggerFactory.getLogger(SSLUtil.class);

	public synchronized static SslContext initServer(File privateKey, File certificate) throws IOException {
		if (sslContext == null) {
			doInit(privateKey, certificate);
		}
		return sslContext;
	}
	
	public synchronized static SslContext initServerHttp2(File privateKey, File certificate) throws IOException {
		if (sslContext == null) {
			doInitHttp2(privateKey, certificate);
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
	
	private static void doInitHttp2(File privateKey, File certificate) throws IOException {

		LoggerUtil.prettyNIOServerLog(logger, "加载证书公钥：{}", certificate.getCanonicalPath());
		LoggerUtil.prettyNIOServerLog(logger, "加载证书私钥：{}", privateKey.getCanonicalPath());

		sslContext = SslContextBuilder.forServer(certificate, privateKey).applicationProtocolConfig(new ApplicationProtocolConfig(Protocol.ALPN,
									// NO_ADVERTISE is currently the
									// only mode supported by both
									// OpenSsl and JDK providers.
									SelectorFailureBehavior.NO_ADVERTISE,
									// ACCEPT is currently the only
									// mode supported by both OpenSsl
									// and JDK providers.
									SelectedListenerFailureBehavior.ACCEPT, 
									ApplicationProtocolNames.HTTP_2,
									ApplicationProtocolNames.HTTP_1_1)).build();

	}

	public static SSLEngine getSslEngine() {
		return sslContext.newEngine();
	}

	public static SslContext getSslContext() {
		return sslContext;
	}

}
