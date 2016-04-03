package com.test.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.gifisan.nio.server.NIOServlet;
import com.gifisan.nio.server.Request;
import com.gifisan.nio.server.Response;
import com.gifisan.nio.server.session.Session;

public class TestUploadServlet extends NIOServlet {

	public void accept(Request request, Response response) throws Exception {
		
		Session session = request.getSession();
		
		OutputStream outputStream = session.getServerOutputStream();
		
		if(outputStream == null){
			
			String fileName = "upload-" + request.getContent();
			
			outputStream = new FileOutputStream(new File(fileName));

			session.setServerOutputStream(outputStream);
		}else{
			
			response.write("上传成功！");
			
			response.flush();
		}
	}
}
