package com.test.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.gifisan.nio.server.session.ServerSession;
import com.gifisan.nio.service.NIOServlet;

public class TestUploadServlet extends NIOServlet {

	public void accept(ServerSession session) throws Exception {
		
		OutputStream outputStream = session.getServerOutputStream();
		
		if(outputStream == null){
			
			String fileName = "upload-" + session.getRequestText();
			
			outputStream = new FileOutputStream(new File(fileName));

			session.setServerOutputStream(outputStream);
		}else{
			
			session.write("上传成功！");
			
			session.flush();
		}
	}
}
