package com.generallycloud.nio.container.protobase.example;

import java.io.File;
import java.io.IOException;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.FileReceiveUtil;
import com.generallycloud.nio.container.FileSendUtil;
import com.generallycloud.nio.container.RESMessage;
import com.generallycloud.nio.container.protobase.service.BaseFutureAcceptorService;

public class TestDownloadServlet extends BaseFutureAcceptorService {

	public static final String SERVICE_NAME = TestDownloadServlet.class.getSimpleName();

	protected void doAccept(SocketSession session, BaseReadFuture future) throws Exception {
		FileSendUtil fileSendUtil = new FileSendUtil();

		File file = new File(future.getParameters().getParameter(FileReceiveUtil.FILE_NAME));

		if (!file.exists()) {
			fileNotFound(session, future, "file not found");
			return;
		}

		fileSendUtil.sendFile(session, future.getFutureName(), file, 1024 * 800);

	}

	private void fileNotFound(SocketSession session, BaseReadFuture future, String msg) throws IOException {
		RESMessage message = new RESMessage(404, msg);
		future.write(message.toString());
		session.flush(future);
	}
}
