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
package com.generallycloud.baseio.codec.http11;

import com.generallycloud.baseio.codec.http11.future.HttpReadFuture;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.concurrent.Waiter;
import com.generallycloud.baseio.protocol.ReadFuture;

public class HttpIOEventHandle extends IoEventHandleAdaptor{
	
	private Waiter<HttpReadFuture> waiter;

	@Override
	public void accept(SocketSession session, ReadFuture future) throws Exception {
		
		HttpReadFuture f = (HttpReadFuture) future;
		
		Waiter<HttpReadFuture> waiter = this.waiter;
		
		if (waiter != null) {
			
			this.waiter = null;
			
			waiter.setPayload(f);
		}
	}

	public void setWaiter(Waiter<HttpReadFuture> waiter) {
		this.waiter = waiter;
	}
}
