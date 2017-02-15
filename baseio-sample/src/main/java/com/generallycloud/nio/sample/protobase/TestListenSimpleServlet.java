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
package com.generallycloud.nio.sample.protobase;

import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFutureImpl;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.protobase.service.ProtobaseFutureAcceptorService;

public class TestListenSimpleServlet extends ProtobaseFutureAcceptorService{
	
	public static final String SERVICE_NAME = TestListenSimpleServlet.class.getSimpleName();
	
	@Override
	protected void doAccept(SocketSession session, ProtobaseReadFuture future) throws Exception {

		String test = future.getReadText();

		if (StringUtil.isNullOrBlank(test)) {
			test = "test";
		}
		
		future.write(test);
		future.write("$");
		session.flush(future);
		
		for (int i = 0; i < 5; i++) {
			
			ProtobaseReadFuture f = new ProtobaseReadFutureImpl(session.getContext(),future.getFutureID(),future.getFutureName());
			
			f.write(test);
			f.write("$");
			
			session.flush(f);
		}
		
	}

}
