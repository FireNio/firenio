package com.test.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.InputStream;
import com.gifisan.nio.server.NIOServlet;
import com.gifisan.nio.server.Request;
import com.gifisan.nio.server.Response;

public class TestUploadServlet extends NIOServlet{

	public void accept(Request request, Response response) throws Exception {
		String fileName = "upload-"+request.getContent();
		InputStream inputStream = request.getInputStream();
		File file = new File(fileName);
		FileOutputStream outputStream = new FileOutputStream(file);
		int BLOCK = 102400;
		ByteBuffer BUFFER = ByteBuffer.allocate(BLOCK);
		
		int length = inputStream.read(BUFFER);
		while (length == BLOCK) {
			outputStream.write(BUFFER.array());
			BUFFER.clear();
			length = inputStream.read(BUFFER);
		}
		
		if (length > 0) {
			outputStream.write(BUFFER.array());
		}
		CloseUtil.close(outputStream);
		response.write("上传成功！");
		response.flush();
	}

	
}
