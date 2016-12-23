/*
 * Copyright 2015 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package com.generallycloud.nio.sample.protobase;

import java.io.File;
import java.io.IOException;

import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.FileReceiveUtil;
import com.generallycloud.nio.container.FileSendUtil;
import com.generallycloud.nio.container.RESMessage;
import com.generallycloud.nio.container.protobase.service.ProtobaseFutureAcceptorService;

public class TestDownloadServlet extends ProtobaseFutureAcceptorService {

	public static final String SERVICE_NAME = TestDownloadServlet.class.getSimpleName();

	@Override
	protected void doAccept(SocketSession session, ProtobaseReadFuture future) throws Exception {
		FileSendUtil fileSendUtil = new FileSendUtil();

		File file = new File(future.getParameters().getParameter(FileReceiveUtil.FILE_NAME));

		if (!file.exists()) {
			fileNotFound(session, future, "file not found");
			return;
		}

		fileSendUtil.sendFile(session, future.getFutureName(), file, 1024 * 800);

	}

	private void fileNotFound(SocketSession session, ProtobaseReadFuture future, String msg) throws IOException {
		RESMessage message = new RESMessage(404, msg);
		future.write(message.toString());
		session.flush(future);
	}
}
