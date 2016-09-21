package com.generallycloud.nio.extend.plugin.jms.client.cmd;

import java.util.HashMap;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.cmd.CmdResponse;
import com.generallycloud.nio.common.cmd.CommandContext;
import com.generallycloud.nio.extend.plugin.jms.client.MessageBrowser;

@Deprecated
public class MessageSizeExecutable extends MQCommandExecutor {

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
