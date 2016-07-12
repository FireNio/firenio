package com.test.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;
import com.gifisan.nio.extend.service.FutureAcceptorService;

public class TestUploadServlet extends FutureAcceptorService {
	
	public static final String SERVICE_NAME = TestUploadServlet.class.getSimpleName();

	protected void doAccept(Session session, NIOReadFuture future) throws Exception {
		
		if (future.hasOutputStream()) {
			
			OutputStream outputStream = future.getOutputStream();
			
			if(outputStream == null){
				
				String fileName = "upload-" + future.getText();
				
				outputStream = new FileOutputStream(new File(fileName));

				future.setOutputStream(outputStream);
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
