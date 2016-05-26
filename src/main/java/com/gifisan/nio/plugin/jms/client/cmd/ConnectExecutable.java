package com.gifisan.nio.plugin.jms.client.cmd;

import java.util.HashMap;

import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.client.ClientTCPConnector;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.common.cmd.CmdResponse;
import com.gifisan.nio.common.cmd.CommandContext;
import com.gifisan.nio.plugin.jms.client.MessageBrowser;
import com.gifisan.nio.plugin.jms.client.impl.DefaultMessageBrowser;

public class ConnectExecutable extends JMSCommandExecutor {

	private Logger	logger		= LoggerFactory.getLogger(ConnectExecutable.class);
	
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
			
			connector = new ClientTCPConnector(host, Integer.valueOf(port),"M");
			
			connector.connect();
			
			ClientSession session = connector.getClientSession();
			
			connector.login(username, password);
			
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
