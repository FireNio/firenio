package com.test.service.nio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.nio.future.NIOReadFuture;
import com.generallycloud.nio.extend.service.NIOFutureAcceptorService;

public class TestUploadServlet extends NIOFutureAcceptorService {
	
	public static final String SERVICE_NAME = TestUploadServlet.class.getSimpleName();

	protected void doAccept(Session session, NIOReadFuture future) throws Exception {
		
		Parameters parameters = future.getParameters();
		
		OutputStream outputStream = (OutputStream) session.getAttachment();
		
		if (outputStream == null) {
			
			String fileName = "upload-" + future.getText();
			
			outputStream = new FileOutputStream(new File(fileName));
			
			session.setAttachment(outputStream);
		}
		
		byte [] data = future.getBinary();
		
		outputStream.write(data);
		
		boolean isEnd = parameters.getBooleanParameter("isEnd");
		
		if (isEnd) {
			
			CloseUtil.close(outputStream);
			
			session.setAttachment(null);
			
			future.write("上传成功！");
			
			session.flush(future);
		}
	}
}
