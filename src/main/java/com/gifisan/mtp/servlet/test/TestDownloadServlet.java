package com.gifisan.mtp.servlet.test;

import java.io.File;
import java.io.FileInputStream;

import com.gifisan.mtp.common.CloseUtil;
import com.gifisan.mtp.server.MTPServlet;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;

public class TestDownloadServlet extends MTPServlet{

	public static String SERVICE_NAME = TestDownloadServlet.class.getSimpleName();
	
	private int BLOCK = 102400;
	
	public void accept(Request request, Response response) throws Exception {
		

		File file = new File("upload-temp.zip");

		FileInputStream inputStream = new FileInputStream(file);
		response.setStreamResponse(inputStream.available());
		byte [] bytes = new byte[BLOCK];
		int length = inputStream.read(bytes);
		while (length == BLOCK) {
			response.write(bytes);
			length = inputStream.read(bytes);
		}
		if (bytes.length > 0) {
			response.write(bytes,0,length);
		}
		CloseUtil.close(inputStream);
		
	}

	
}
