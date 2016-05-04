package com.gifisan.nio.jms.client.cmd;

import java.util.HashMap;

import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.common.cmd.CmdResponse;
import com.gifisan.nio.common.cmd.CommandContext;
import com.gifisan.nio.jms.client.MessageBrowser;

public class MessageSizeExecutable extends JMSCommandExecutor {

	public CmdResponse exec(CommandContext context, HashMap<String, String> params) {

		CmdResponse response = new CmdResponse();

		MessageBrowser browser = getMessageBrowser(context);

		if (browser == null) {
			response.setResponse("请先登录！");
			return response;
		}
		
		try {
			int size = browser.size();
			response.setResponse(String.valueOf(size));
		} catch (Exception e) {
			browser.logout();
			response.setResponse(e.getMessage());
			DebugUtil.debug(e);
		}
		
		return response;
	}
}
