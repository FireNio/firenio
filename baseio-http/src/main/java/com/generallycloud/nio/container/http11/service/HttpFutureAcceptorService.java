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
package com.generallycloud.nio.container.http11.service;

import com.generallycloud.nio.codec.http11.HttpContext;
import com.generallycloud.nio.codec.http11.HttpSession;
import com.generallycloud.nio.codec.http11.HttpSessionManager;
import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.codec.http11.future.HttpStatus;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.service.FutureAcceptorService;
import com.generallycloud.nio.protocol.ReadFuture;

public abstract class HttpFutureAcceptorService extends FutureAcceptorService {
	
	private HttpContext		context	= HttpContext.getInstance();

	@Override
	public void accept(SocketSession session, ReadFuture future) throws Exception {

		HttpSessionManager manager = context.getHttpSessionManager();

		HttpReadFuture httpReadFuture = (HttpReadFuture) future;

		HttpSession httpSession = manager.getHttpSession(context,session, httpReadFuture);

		this.doAccept(httpSession, httpReadFuture);
	}

	protected abstract void doAccept(HttpSession session, HttpReadFuture future) throws Exception;

	@Override
	public void exceptionCaught(SocketSession session, ReadFuture future, Exception cause, IoEventState state) {
		
		if (state == IoEventState.HANDLE) {
			
			if (future instanceof HttpReadFuture) {
				((HttpReadFuture)future).setStatus(HttpStatus.C500);
			}
			
			future.write("server error:"+cause.getMessage());

			session.flush(future);
		}
	}
}
