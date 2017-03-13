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
package com.generallycloud.baseio.container;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.OnReadFuture;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.protocol.NamedReadFuture;
import com.generallycloud.baseio.protocol.ReadFuture;

public class SimpleIoEventHandle extends IoEventHandleAdaptor {

	private Map<String, OnReadFutureWrapper>	listeners	= new HashMap<String, OnReadFutureWrapper>();

	@Override
	public void accept(SocketSession session, ReadFuture future) throws Exception {

		NamedReadFuture f = (NamedReadFuture) future;

		OnReadFutureWrapper onReadFuture = listeners.get(f.getFutureName());

		if (onReadFuture != null) {
			onReadFuture.onResponse(session, f);
		}
	}
	
	public void listen(String serviceName, OnReadFuture onReadFuture) throws IOException {

		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IOException("empty service name");
		}

		OnReadFutureWrapper wrapper = listeners.get(serviceName);

		if (wrapper == null) {

			wrapper = new OnReadFutureWrapper();

			listeners.put(serviceName, wrapper);
		}

		if (onReadFuture == null) {
			return;
		}

		wrapper.setListener(onReadFuture);
	}
	
	public OnReadFutureWrapper getOnReadFutureWrapper(String serviceName){
		return listeners.get(serviceName);
	}
	
	public void putOnReadFutureWrapper(String serviceName,OnReadFutureWrapper wrapper){
		listeners.put(serviceName, wrapper);
	}
}
