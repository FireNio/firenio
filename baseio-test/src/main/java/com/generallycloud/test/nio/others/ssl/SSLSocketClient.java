package com.generallycloud.test.nio.others.ssl;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SSLSocketClient {

	public static void main(String [] args) throws Exception {

		X509TrustManager x509m = new X509TrustManager() {

			public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
					throws java.security.cert.CertificateException {

			}

			public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
					throws java.security.cert.CertificateException {

			}

			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};
		SSLContext context = SSLContext.getInstance("SSL");
		// 初始化
		context.init(null, new TrustManager[] { x509m }, new SecureRandom());
		SSLSocketFactory factory = context.getSocketFactory();
		SSLSocket s = (SSLSocket) factory.createSocket("localhost", 443);
		System.out.println("ok");

		OutputStream output = s.getOutputStream();
		InputStream input = s.getInputStream();

		output.write("alert".getBytes());
		System.out.println("sent: alert");
		output.flush();

		byte[] buf = new byte[1024];
		int len = input.read(buf);
		System.out.println("received:" + new String(buf, 0, len));
	}
}
