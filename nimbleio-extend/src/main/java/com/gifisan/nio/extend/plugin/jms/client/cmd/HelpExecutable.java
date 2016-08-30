package com.gifisan.nio.extend.plugin.jms.client.cmd;

import java.util.HashMap;

import com.gifisan.nio.common.cmd.CmdResponse;
import com.gifisan.nio.common.cmd.CommandContext;

public class HelpExecutable extends MQCommandExecutor {

	public CmdResponse exec(CommandContext context, HashMap<String, String> params) {

		CmdResponse response = new CmdResponse();
		
		response.setResponse("请尝试："+context.getCommandKeys());
		
		return response;
	}
}
