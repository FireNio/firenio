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
package com.generallycloud.nio.container.http11.startup;

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.codec.http11.ServerHTTPProtocolFactory;
import com.generallycloud.nio.codec.http11.future.Cookie;
import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.common.DebugUtil;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.common.UUIDGenerator;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSocketSEListener;
import com.generallycloud.nio.component.NioSocketChannelContext;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.protocol.ReadFuture;

public class HttpServerLoadStartup {

	public static void main(String[] args) throws Exception {

		IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

			@Override
			public void accept(SocketSession session, ReadFuture future) throws Exception {

				HttpReadFuture f = (HttpReadFuture) future;
				
				String token = f.getCookie("__token");

				if (StringUtil.isNullOrBlank(token)) {
					token = UUIDGenerator.random();
					Cookie c = new Cookie("__token", token);
					c.setMaxAge(99999);
					f.addCookie(c);
					DebugUtil.debug("___________add token:"+token);
				}else{
					DebugUtil.debug("___________get token:"+token);
				}
				
				String res = "yes server already accept your message :) " + f.getRequestParams();

				f.write(res);
				
				session.flush(f);
			}
		};

		ServerConfiguration configuration = new ServerConfiguration(80);
		
		SocketChannelContext context = new NioSocketChannelContext(configuration);
		
		context.setIoEventHandleAdaptor(eventHandleAdaptor);
		
		context.addSessionEventListener(new LoggerSocketSEListener());
		
		context.setProtocolFactory(new ServerHTTPProtocolFactory());

		SocketChannelAcceptor acceptor = new SocketChannelAcceptor(context);

		try {
			acceptor.bind();
		} catch (Exception e) {
			acceptor.unbind();
			throw e;
		}
	}
}
