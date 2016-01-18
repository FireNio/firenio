package com.gifisan.mtp.servlet.test;

import java.io.File;
import java.io.FileOutputStream;

import com.gifisan.mtp.common.CloseUtil;
import com.gifisan.mtp.component.MTPRequestInputStream;
import com.gifisan.mtp.server.MTPServlet;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;

public class TestUploadServlet extends MTPServlet{

	public void accept(Request request, Response response) throws Exception {
		String fileName = "upload-"+request.getContent();
		MTPRequestInputStream inputStream = request.getInputStream();
		File file = new File(fileName);
		FileOutputStream outputStream = new FileOutputStream(file);
		int BLOCK = 102400;
		byte [] bytes = inputStream.read(BLOCK);
		while (bytes.length == BLOCK) {
			outputStream.write(bytes);
			bytes = inputStream.read(BLOCK);
		}
		if (bytes.length > 0) {
			outputStream.write(bytes,0,bytes.length);
		}
		CloseUtil.close(outputStream);
		response.write("上传成功！");
		response.flush();
	}

	
}
