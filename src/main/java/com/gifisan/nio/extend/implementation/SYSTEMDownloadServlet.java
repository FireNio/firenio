package com.gifisan.nio.extend.implementation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;
import com.gifisan.nio.extend.RESMessage;
import com.gifisan.nio.extend.service.NIOFutureAcceptorService;

public class SYSTEMDownloadServlet extends NIOFutureAcceptorService {

	public static final String	SERVICE_NAME	= SYSTEMDownloadServlet.class.getSimpleName();

	private Logger				logger		= LoggerFactory.getLogger(SYSTEMDownloadServlet.class);

	public void doAccept(Session session, NIOReadFuture future) throws Exception {

		Parameters param = future.getParameters();
		
		String fileName = param.getParameter("fileName");

		if (StringUtil.isNullOrBlank(fileName) || fileName.length() > 128) {
			fileNotFound(session, future, "file not found:" + fileName);
			return;
		}

		int start = param.getIntegerParameter("start");

		int downloadLength = param.getIntegerParameter("length");

		File file = new File(fileName);

		if (!file.exists()) {
			fileNotFound(session, future, "file not found:" + fileName);
			return;
		}
		
		try {

			FileInputStream inputStream = new FileInputStream(file);

			int available = inputStream.available();

			if (downloadLength == 0) {
				downloadLength = available - start;
			}

			future.setInputStream(inputStream);
			
			future.write("下载成功！");

			session.flush(future);

		} catch (IOException e) {
			logger.debug(e);
			fileNotFound(session, future, e.getMessage());
		}
	}
	
	private void fileNotFound(Session session,ReadFuture future,String msg){
		RESMessage message = new RESMessage(404, msg);
		future.write(message.toString());
		session.flush(future);
	}
}
