package com.generallycloud.nio.extend.plugin.jms.client.cmd;

import java.util.HashMap;

import com.generallycloud.nio.common.cmd.CmdResponse;
import com.generallycloud.nio.common.cmd.Command;
import com.generallycloud.nio.common.cmd.CommandContext;
import com.generallycloud.nio.connector.SocketChannelConnector;

@Deprecated
public class MQCommand extends Command {

	private HelpExecutable	helpExecutable	= new HelpExecutable();

	public static void main(String[] args) {

		CommandContext context = new CommandContext();
		
		MQCommand command = new MQCommand();
		
		context.registExecutable("browser", new BrowserExecutable());
		context.registExecutable("connect", new ConnectExecutable());
		context.registExecutable("disconnect", new DisconnectExecutable());
		context.registExecutable("exit", new ExitExecutable());
		context.registExecutable("help", command.helpExecutable);
		context.registExecutable("?", command.helpExecutable);
		context.registExecutable("size", new MessageSizeExecutable());
		
		command.execute(context);
	}

	public void printPrefix(CommandContext context) {

		SocketChannelConnector connector = (SocketChannelConnector) context.getAttribute("KEY_CONNECTOR");

		if (connector == null) {
			System.out.print("未连接> ");
		} else {
			System.out.print(connector + "> ");
		}
	}

	public CmdResponse doHelp(CommandContext context, HashMap<String, String> params) {

		return helpExecutable.exec(context, params);
	}
}
