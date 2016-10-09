package com.generallycloud.nio.extend.implementation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.RESMessage;
import com.generallycloud.nio.extend.service.NIOFutureAcceptorService;
import com.generallycloud.nio.protocol.ReadFuture;

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

//			future.setInputStream(inputStream);//FIXME download
			
			future.write("下载成功！");

			session.flush(future);

		} catch (IOException e) {
			logger.debug(e);
			fileNotFound(session, future, e.getMessage());
		}
		
	}
	
	private void fileNotFound(Session session,ReadFuture future,String msg) throws IOException{
		RESMessage message = new RESMessage(404, msg);
		future.write(message.toString());
		session.flush(future);
	}
}
