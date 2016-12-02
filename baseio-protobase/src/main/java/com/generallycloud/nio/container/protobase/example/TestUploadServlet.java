package com.generallycloud.nio.container.protobase.example;

import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.FileReceiveUtil;
import com.generallycloud.nio.container.protobase.service.ProtobaseFutureAcceptorService;

public class TestUploadServlet extends ProtobaseFutureAcceptorService {

	public static final String	SERVICE_NAME		= TestUploadServlet.class.getSimpleName();

	private FileReceiveUtil		fileReceiveUtil	= new FileReceiveUtil("upload-");

	protected void doAccept(SocketSession session, ProtobaseReadFuture future) throws Exception {

		fileReceiveUtil.accept(session, future, true);
	}
}
