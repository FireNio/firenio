/*
 * Copyright 2015-2017 GenerallyCloud.com
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
package com.generallycloud.test.nio.common;

import com.generallycloud.baseio.codec.http11.future.ClientHttpFuture;
import com.generallycloud.baseio.codec.http11.future.HttpFuture;
import com.generallycloud.baseio.codec.protobase.future.ProtobaseFuture;
import com.generallycloud.baseio.codec.protobase.future.ProtobaseFutureImpl;
import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.SocketSession;

public class FutureFactory {

	public static ProtobaseFuture create(SocketSession session, ProtobaseFuture future) {
		ProtobaseFuture readFuture = future;
		return create(session, readFuture.getFutureId(), readFuture.getFutureName(), readFuture.getIoEventHandle());
	}

	public static ProtobaseFuture create(SocketSession session, int futureID, String serviceName,
			IoEventHandle ioEventHandle) {

		ProtobaseFutureImpl textReadFuture = new ProtobaseFutureImpl(session.getContext(),futureID, serviceName);

		textReadFuture.setIoEventHandle(ioEventHandle);

		return textReadFuture;
	}

	public static ProtobaseFuture create(SocketSession session, int futureID, String serviceName) {

		return create(session, futureID, serviceName, session.getContext().getIoEventHandleAdaptor());
	}

	public static ProtobaseFuture create(SocketSession session, String serviceName, IoEventHandle ioEventHandle) {

		return create(session, 0, serviceName, ioEventHandle);
	}

	public static HttpFuture createHttpReadFuture(SocketSession session, String url) {
		return new ClientHttpFuture(session.getContext(),url, "GET");
	}

	public static ProtobaseFuture create(SocketSession session, String serviceName) {

		return create(session, 0, serviceName, session.getContext().getIoEventHandleAdaptor());
	}
}
