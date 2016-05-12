package com.gifisan.nio.jms.client.cmd;

import java.util.HashMap;

import com.gifisan.nio.client.ClientTCPConnector;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.cmd.CmdResponse;
import com.gifisan.nio.common.cmd.CommandContext;
import com.gifisan.nio.jms.client.MessageBrowser;

public class DisconnectExecutable extends JMSCommandExecutor {

	public CmdResponse exec(CommandContext context, HashMap<String, String> params) {

		CmdResponse response = new CmdResponse();

		MessageBrowser browser = getMessageBrowser(context);

		if (browser == null) {
			response.setResponse("请先登录！");
			return response;
		}
		
		browser.logout();
		
		ClientTCPConnector connector = getClientConnector(context);
		
		CloseUtil.close(connector);
		
		setMessageBrowser(context, null);
		setClientConnector(context, null);
		
		response.setResponse("已断开连接！");
		return response;
	}
}
