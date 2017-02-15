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
package com.generallycloud.nio.container.jms.client.cmd;

import java.util.HashMap;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.common.cmd.CmdResponse;
import com.generallycloud.nio.common.cmd.CommandContext;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketChannelContextImpl;
import com.generallycloud.nio.component.LoggerSocketSEListener;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.container.FixedSession;
import com.generallycloud.nio.container.SimpleIOEventHandle;
import com.generallycloud.nio.container.jms.client.MessageBrowser;
import com.generallycloud.nio.container.jms.client.impl.DefaultMessageBrowser;

@Deprecated
public class ConnectExecutable extends MQCommandExecutor {

	private Logger	logger		= LoggerFactory.getLogger(ConnectExecutable.class);
	
	@Override
	public CmdResponse exec(CommandContext context, HashMap<String, String> params) {

		CmdResponse response = new CmdResponse();
		
		SocketChannelConnector connector = getClientConnector(context);
		
		if (connector != null) {
			response.setResponse("已登录。");
			return response;
		}

		String username = params.get("-un");
		String password = params.get("-p");
		String host     = params.get("-host");
		String port     = params.get("-port");
		
		if (StringUtil.isNullOrBlank(username) 
				|| StringUtil.isNullOrBlank(password)
				|| StringUtil.isNullOrBlank(host)
				|| StringUtil.isNullOrBlank(port)) {
			response.setResponse("参数不正确！\n"
									+"example:\n"
									+"connect -host:localhost -port:8300 -un:admin -p:admin100");
			return response;
		}
		
		try {
			
			SocketChannelContext baseContext = new SocketChannelContextImpl(new ServerConfiguration(Integer.parseInt(port)));
			
			connector = new SocketChannelConnector(baseContext);

//			String serviceName = SYSTEMStopServerServlet.SERVICE_NAME;

			SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();
			
			baseContext.setIoEventHandleAdaptor(eventHandle);

			baseContext.addSessionEventListener(new LoggerSocketSEListener());

			FixedSession session = new FixedSession(connector.connect());
			
			//FIXME denglu cuowu 
			session.login(username, password);
			
			MessageBrowser browser = new DefaultMessageBrowser(session);
			
			response.setResponse("连接成功！");
			
			setClientConnector(context, connector);
			setMessageBrowser(context, browser);
			
		} catch (Exception e) {
			setClientConnector(context, null);
			setMessageBrowser(context, null);
			response.setResponse(e.getMessage());
			//debug
			logger.debug(e);
		}
		return response;
	}
}
