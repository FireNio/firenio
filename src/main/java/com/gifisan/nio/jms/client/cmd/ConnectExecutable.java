package com.gifisan.nio.jms.client.cmd;

import java.util.HashMap;

import com.gifisan.nio.client.ClientTCPConnector;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.common.cmd.CmdResponse;
import com.gifisan.nio.common.cmd.CommandContext;
import com.gifisan.nio.jms.client.MessageBrowser;
import com.gifisan.nio.jms.client.impl.MessageBrowserImpl;

public class ConnectExecutable extends JMSCommandExecutor {

	public CmdResponse exec(CommandContext context, HashMap<String, String> params) {

		CmdResponse response = new CmdResponse();
		
		ClientTCPConnector connector = getClientConnector(context);
		
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
			
			connector = new ClientTCPConnector(host, Integer.valueOf(port));
			
			connector.connect();
			
			ClientSession session = connector.getClientSession();
			
			MessageBrowser browser = new MessageBrowserImpl(session);
			
			browser.login(username, password);
			
			response.setResponse("连接成功！");
			
			setClientConnector(context, connector);
			setMessageBrowser(context, browser);
			
		} catch (Exception e) {
			setClientConnector(context, null);
			setMessageBrowser(context, null);
			response.setResponse(e.getMessage());
			//debug
			DebugUtil.debug(e);
		}
		return response;
	}
}
