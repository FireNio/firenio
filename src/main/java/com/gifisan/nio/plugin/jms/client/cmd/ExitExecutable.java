package com.gifisan.nio.plugin.jms.client.cmd;

import java.util.HashMap;

import com.gifisan.nio.client.ClientTCPConnector;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.cmd.CmdResponse;
import com.gifisan.nio.common.cmd.CommandContext;
import com.gifisan.nio.plugin.jms.client.MessageBrowser;

public class ExitExecutable extends JMSCommandExecutor {

	public CmdResponse exec(CommandContext context, HashMap<String, String> params) {

		CmdResponse response = new CmdResponse();

		MessageBrowser browser = getMessageBrowser(context);

		if (browser != null) {
			browser.logout();
		}
		
		ClientTCPConnector connector = getClientConnector(context);
		CloseUtil.close(connector);
		
		setMessageBrowser(context, null);
		setClientConnector(context, null);
		
		response.setContinue(false);
		response.setResponse("系统退出！");
		
		return response;
	}
}
