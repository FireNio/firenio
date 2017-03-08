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
package com.generallycloud.baseio.container.protobase.startup;

import com.generallycloud.baseio.acceptor.SocketChannelAcceptor;
import com.generallycloud.baseio.codec.protobase.ProtobaseProtocolFactory;
import com.generallycloud.baseio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.baseio.common.SharedBundle;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.LoggerSocketSEListener;
import com.generallycloud.baseio.component.NioSocketChannelContext;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.configuration.PropertiesSCLoader;
import com.generallycloud.baseio.configuration.ServerConfiguration;
import com.generallycloud.baseio.protocol.ReadFuture;

public class ProtobaseServerLoadStartup {

	public static void main(String[] args) throws Exception {
		
		SharedBundle.instance().loadAllProperties("nio");
		
		IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

			@Override
			public void accept(SocketSession session, ReadFuture future) throws Exception {
				ProtobaseReadFuture f = (ProtobaseReadFuture)future;
				f.write("yes server already accept your message");
				f.write(f.getReadText());
				session.flush(future);
			}
		};

		PropertiesSCLoader loader = new PropertiesSCLoader();
		
		ServerConfiguration configuration = loader.loadConfiguration(SharedBundle.instance());

		SocketChannelContext context = new NioSocketChannelContext(configuration);

		SocketChannelAcceptor acceptor = new SocketChannelAcceptor(context);

		try {

			context.setIoEventHandleAdaptor(eventHandleAdaptor);

			context.addSessionEventListener(new LoggerSocketSEListener());

			acceptor.getContext().setProtocolFactory(new ProtobaseProtocolFactory());

			acceptor.bind();

		} catch (Throwable e) {

			acceptor.unbind();

			throw new RuntimeException(e);
		}
	}
	
}
