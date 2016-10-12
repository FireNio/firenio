package com.test.service.nio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.service.NIOFutureAcceptorService;

public class TestUploadServlet extends NIOFutureAcceptorService {

	public static final String FILE_NAME = "file-name";
	
	public static final String IS_END = "isEnd";
	
	public static final String	SERVICE_NAME	= TestUploadServlet.class.getSimpleName();
	
	private String UPLOAD_FILE = "upload-file";
	
	private Logger logger = LoggerFactory.getLogger(TestUploadServlet.class);

	protected void doAccept(Session session, NIOReadFuture future) throws Exception {

		Parameters parameters = future.getParameters();

		OutputStream outputStream = (OutputStream) session.getAttribute(UPLOAD_FILE);

		if (outputStream == null) {

			String fileName = "upload-" + parameters.getParameter(FILE_NAME);

			outputStream = new FileOutputStream(new File(fileName));

			session.setAttribute(UPLOAD_FILE, outputStream);
		}
		
		byte[] data = future.getBinary();

		outputStream.write(data,0,future.getBinaryLength());
		
		logger.info("upload...................{}",future.getBinaryLength());
		
		outputStream.flush();

		boolean isEnd = parameters.getBooleanParameter(IS_END);

		if (isEnd) {
			
			CloseUtil.close(outputStream);
			
			session.removeAttribute(UPLOAD_FILE);

			future.write("上传成功！");

			session.flush(future);
		}
	}
}
