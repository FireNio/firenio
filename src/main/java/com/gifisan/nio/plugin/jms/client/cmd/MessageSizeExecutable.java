package com.gifisan.nio.plugin.jms.client.cmd;

import java.util.HashMap;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.cmd.CmdResponse;
import com.gifisan.nio.common.cmd.CommandContext;
import com.gifisan.nio.plugin.jms.client.MessageBrowser;

public class MessageSizeExecutable extends JMSCommandExecutor {

	private Logger	logger	= LoggerFactory.getLogger(MessageSizeExecutable.class);

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
			response.setResponse(e.getMessage());
			logger.debug(e);
		}

		return response;
	}
}
