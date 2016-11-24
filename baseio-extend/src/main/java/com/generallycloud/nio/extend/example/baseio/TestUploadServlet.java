package com.generallycloud.nio.extend.example.baseio;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.extend.FileReceiveUtil;
import com.generallycloud.nio.extend.service.BaseFutureAcceptorService;

public class TestUploadServlet extends BaseFutureAcceptorService {

	public static final String	SERVICE_NAME		= TestUploadServlet.class.getSimpleName();

	private FileReceiveUtil		fileReceiveUtil	= new FileReceiveUtil("upload-");

	protected void doAccept(SocketSession session, BaseReadFuture future) throws Exception {

		fileReceiveUtil.accept(session, future, true);
	}
}
