package com.gifisan.nio.plugin.jms.client.cmd;

import java.util.HashMap;

import com.gifisan.nio.client.ClientTCPConnector;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.cmd.CmdResponse;
import com.gifisan.nio.common.cmd.CommandContext;

public class DisconnectExecutable extends JMSCommandExecutor {

	public CmdResponse exec(CommandContext context, HashMap<String, String> params) {

		CmdResponse response = new CmdResponse();

		ClientTCPConnector connector = getClientConnector(context);
		
		if (connector == null) {
			response.setResponse("请先登录！");
			return response;
		}
		
		connector.logout();
		
		CloseUtil.close(connector);
		
		setMessageBrowser(context, null);
		setClientConnector(context, null);
		
		response.setResponse("已断开连接！");
		return response;
	}
}
