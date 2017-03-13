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
package com.generallycloud.test.nio.protobase;

import com.generallycloud.baseio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.SharedBundle;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.OnReadFuture;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.connector.SocketChannelConnector;
import com.generallycloud.baseio.container.FixedSession;
import com.generallycloud.baseio.container.SimpleIoEventHandle;
import com.generallycloud.baseio.protocol.ReadFuture;
import com.generallycloud.test.nio.common.IoConnectorUtil;

public class TestListenSimple {
	
	
	public static void main(String[] args) throws Exception {

		SharedBundle.instance().loadAllProperties("nio");
		String serviceKey = "TestListenSimpleServlet";
		String param = "ttt";
		
		SimpleIoEventHandle eventHandle = new SimpleIoEventHandle();

		SocketChannelConnector connector = IoConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = new FixedSession(connector.connect());

		ProtobaseReadFuture future = session.request(serviceKey, param);
		System.out.println(future.getReadText());
		
		session.listen(serviceKey,new OnReadFuture() {
			
			@Override
			public void onResponse(SocketSession session, ReadFuture future) {
				ProtobaseReadFuture f = (ProtobaseReadFuture) future;
				System.out.println(f.getReadText());
			}
		});
		
		session.write(serviceKey, param);
		
		ThreadUtil.sleep(1000);
		CloseUtil.close(connector);
		
	}
}
