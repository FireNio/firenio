package com.generallycloud.nio.extend.implementation;

import java.io.File;
import java.io.IOException;

import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.FileReceiveUtil;
import com.generallycloud.nio.extend.FileSendUtil;
import com.generallycloud.nio.extend.RESMessage;
import com.generallycloud.nio.extend.service.NIOFutureAcceptorService;
import com.generallycloud.nio.protocol.ReadFuture;

public class SYSTEMDownloadServlet extends NIOFutureAcceptorService {

	public static final String	SERVICE_NAME	= SYSTEMDownloadServlet.class.getSimpleName();

	private Logger				logger		= LoggerFactory.getLogger(SYSTEMDownloadServlet.class);

	public void doAccept(Session session, NIOReadFuture future) throws Exception {

		FileSendUtil fileSendUtil = new FileSendUtil();

		File file = new File(future.getParameters().getParameter(FileReceiveUtil.FILE_NAME));

		if (!file.exists()) {
			fileNotFound(session, future, "file not found");
			return;
		}

		fileSendUtil.sendFile(session, future.getFutureName(), file, 1024 * 800);

	}

	private void fileNotFound(Session session, ReadFuture future, String msg) throws IOException {
		RESMessage message = new RESMessage(404, msg);
		future.write(message.toString());
		session.flush(future);
	}
}
