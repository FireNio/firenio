package com.test.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.gifisan.nio.component.ReadFuture;
import com.gifisan.nio.server.session.IOSession;
import com.gifisan.nio.service.NIOServlet;

public class TestUploadServlet extends NIOServlet {

	public void accept(IOSession session,ReadFuture future) throws Exception {
		
		OutputStream outputStream = future.getOutputStream();
		
		if(outputStream == null){
			
			String fileName = "upload-" + future.getText();
			
			outputStream = new FileOutputStream(new File(fileName));

			future.setIOEvent(outputStream, null);
		}else{
			
			session.write("上传成功！");
			
			session.flush();
		}
	}
}
