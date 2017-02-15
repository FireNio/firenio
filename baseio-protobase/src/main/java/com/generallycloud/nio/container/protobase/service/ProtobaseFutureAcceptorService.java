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
package com.generallycloud.nio.container.protobase.service;

import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.service.FutureAcceptorService;
import com.generallycloud.nio.protocol.ReadFuture;

public abstract class ProtobaseFutureAcceptorService extends FutureAcceptorService {

	@Override
	public void accept(SocketSession session, ReadFuture future) throws Exception {
		this.doAccept(session, (ProtobaseReadFuture) future);
	}

	protected abstract void doAccept(SocketSession session, ProtobaseReadFuture future) throws Exception;

	@Override
	public void exceptionCaught(SocketSession session, ReadFuture future, Exception cause, IoEventState state) {

		if (state == IoEventState.HANDLE) {

			future.write(cause.getClass().getName() + ":" + cause.getMessage());
			
			session.flush(future);
		}
		
		super.exceptionCaught(session, future, cause, state);
	}
}
