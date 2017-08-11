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
package com.generallycloud.sample.baseio.http11;

import java.io.IOException;

import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.service.FutureAcceptorFilter;
import com.generallycloud.baseio.protocol.NamedFuture;

public class TestHttpExceptionFilter extends FutureAcceptorFilter {

	@Override
	protected void accept(SocketSession session, NamedFuture future) throws Exception {
		
		if ("/test-error-filter".equals(future.getFutureName())) {
			throw new IOException("test-error-filter222");
		}
		
	}

}
