package com.test.servlet;

import java.io.File;
import java.io.FileOutputStream;

import com.gifisan.nio.common.StreamUtil;
import com.gifisan.nio.component.InputStream;
import com.gifisan.nio.server.NIOServlet;
import com.gifisan.nio.server.Request;
import com.gifisan.nio.server.Response;

public class TestUploadServlet extends NIOServlet {

	public void accept(Request request, Response response) throws Exception {
		String fileName = "upload-" + request.getContent();
		
		InputStream inputStream = request.getInputStream();
		
		FileOutputStream outputStream = new FileOutputStream(new File(fileName));

		StreamUtil.write(inputStream, outputStream, 102400);

		response.write("上传成功！");
		
		response.flush();
	}

}
