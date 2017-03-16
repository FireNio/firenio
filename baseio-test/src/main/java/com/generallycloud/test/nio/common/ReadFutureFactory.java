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

import com.generallycloud.baseio.codec.http11.future.ClientHttpReadFuture;
import com.generallycloud.baseio.codec.http11.future.HttpReadFuture;
import com.generallycloud.baseio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.baseio.codec.protobase.future.ProtobaseReadFutureImpl;
import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.SocketSession;

public class ReadFutureFactory {

	public static ProtobaseReadFuture create(SocketSession session, ProtobaseReadFuture future) {
		ProtobaseReadFuture readFuture = future;
		return create(session, readFuture.getFutureId(), readFuture.getFutureName(), readFuture.getIoEventHandle());
	}

	public static ProtobaseReadFuture create(SocketSession session, Integer futureID, String serviceName,
			IoEventHandle ioEventHandle) {

		ProtobaseReadFutureImpl textReadFuture = new ProtobaseReadFutureImpl(session.getContext(),futureID, serviceName);

		textReadFuture.setIoEventHandle(ioEventHandle);

		return textReadFuture;
	}

	public static ProtobaseReadFuture create(SocketSession session, Integer futureID, String serviceName) {

		return create(session, futureID, serviceName, session.getContext().getIoEventHandleAdaptor());
	}

	public static ProtobaseReadFuture create(SocketSession session, String serviceName, IoEventHandle ioEventHandle) {

		return create(session, 0, serviceName, ioEventHandle);
	}

	public static HttpReadFuture createHttpReadFuture(SocketSession session, String url) {
		return new ClientHttpReadFuture(session.getContext(),url, "GET");
	}

	public static ProtobaseReadFuture create(SocketSession session, String serviceName) {

		return create(session, 0, serviceName, session.getContext().getIoEventHandleAdaptor());
	}
}
