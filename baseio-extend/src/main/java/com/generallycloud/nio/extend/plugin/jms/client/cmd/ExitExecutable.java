package com.generallycloud.nio.extend.plugin.jms.client.cmd;

import java.util.HashMap;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.cmd.CmdResponse;
import com.generallycloud.nio.common.cmd.CommandContext;
import com.generallycloud.nio.connector.IOConnector;

@Deprecated
public class ExitExecutable extends MQCommandExecutor {

	public CmdResponse exec(CommandContext context, HashMap<String, String> params) {

		CmdResponse response = new CmdResponse();

		IOConnector connector = getClientConnector(context);
		
		if (connector == null) {
			response.setResponse("请先登录！");
			return response;
		}
		
		//FIXME logout
//		connector.logout();
		
		CloseUtil.close(connector);
		
		setMessageBrowser(context, null);
		setClientConnector(context, null);
		
		response.setContinue(false);
		response.setResponse("系统退出！");
		
		return response;
	}
}
