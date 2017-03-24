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
package com.generallycloud.baseio.component;

import com.generallycloud.baseio.common.Logger;
import com.generallycloud.baseio.common.LoggerFactory;
import com.generallycloud.baseio.protocol.ReadFuture;

public abstract class IoEventHandleAdaptor implements IoEventHandle {

	private Logger		logger	= LoggerFactory.getLogger(getClass());

	@Override
	public void exceptionCaught(SocketSession session, ReadFuture future, Exception cause, IoEventState state) {
		logger.errorDebug(cause);
	}

	@Override
	public void futureSent(SocketSession session, ReadFuture future) {
		
	}

	protected void initialize(SocketChannelContext context) throws Exception {

	}

	protected void destroy(SocketChannelContext context) throws Exception {

	}

}
