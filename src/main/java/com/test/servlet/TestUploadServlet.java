package com.test.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.service.NIOServlet;

public class TestUploadServlet extends NIOServlet {
	
	public static final String SERVICE_NAME = TestUploadServlet.class.getSimpleName();

	public void accept(IOSession session,ReadFuture future) throws Exception {
		
		if (future.hasOutputStream()) {
			
			OutputStream outputStream = future.getOutputStream();
			
			if(outputStream == null){
				
				String fileName = "upload-" + future.getText();
				
				outputStream = new FileOutputStream(new File(fileName));

				future.setOutputIOEvent(outputStream, null);
			}else{
				
				CloseUtil.close(outputStream);
				
				future.write("上传成功！");
				
				session.flush(future);
			}
		}else{
			
			future.write("上传失败！");
			
			session.flush(future);
		}
		
		
		
	}
}
