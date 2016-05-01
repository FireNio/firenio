package com.test.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.service.NIOServlet;

public class TestUploadServlet extends NIOServlet {

	public void accept(IOSession session,ServerReadFuture future) throws Exception {
		
		OutputStream outputStream = future.getOutputStream();
		
		if(outputStream == null){
			
			String fileName = "upload-" + future.getText();
			
			outputStream = new FileOutputStream(new File(fileName));

			future.setOutputIOEvent(outputStream, null);
		}else{
			
			future.write("上传成功！");
			
			session.flush(future);
		}
	}
}
