package com.generallycloud.nio.common.ssl;

import java.io.File;

import javax.net.ssl.SSLEngine;

import com.generallycloud.nio.common.SharedBundle;

public class SSLUtil {

	static SslContext	sslContext;

	static {

		init();
	}

	public static SslContext init() {
		if (sslContext == null) {
			doInit();
		}
		
		return sslContext;
	}

	private static void doInit() {
		try {
//			SelfSignedCertificate ssc = new SelfSignedCertificate();
//			sslContext = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
			
			File certificate = SharedBundle.instance().loadFile("http/conf/generallycloud.com.crt");
			File privateKey = SharedBundle.instance().loadFile("http/conf/generallycloud.com.key");
			

//			File certificate = SharedBundle.instance().loadFile("http/conf/keyutil_example.com1.crt");
//			File privateKey = SharedBundle.instance().loadFile("http/conf/keyutil_example.com1.key");
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
