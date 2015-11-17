package com.yoocent.mtp.servlet.test;

import java.io.File;
import java.io.FileInputStream;

import com.yoocent.mtp.common.CloseUtil;
import com.yoocent.mtp.server.MTPServlet;
import com.yoocent.mtp.server.Request;
import com.yoocent.mtp.server.Response;

public class TestDownloadServlet extends MTPServlet{

	public static String SERVICE_KEY = TestDownloadServlet.class.getSimpleName();
	
	private int BLOCK = 1024;
	
	public void accept(Request request, Response response) throws Exception {
		
		File file = new File("lib.zip");
		FileInputStream inputStream = new FileInputStream(file);
		response.setStreamResponse(inputStream.available());
		byte [] bytes = new byte[BLOCK];
		int length = inputStream.read(bytes);
		while (length == BLOCK) {
			response.write(bytes);
			response.flush();
			length = inputStream.read(bytes);
		}
		if (bytes.length > 0) {
			response.write(bytes,0,length);
			response.flush();
		}
		CloseUtil.close(inputStream);
		
	}

	
}
