package com.generallycloud.nio.extend.plugin.jms.client.cmd;

import com.generallycloud.nio.common.cmd.CommandContext;
import com.generallycloud.nio.common.cmd.Executable;
import com.generallycloud.nio.connector.IOConnector;
import com.generallycloud.nio.connector.TCPConnector;
import com.generallycloud.nio.extend.plugin.jms.client.MessageBrowser;

@Deprecated
public abstract class MQCommandExecutor implements Executable {

	private String		KEY_CONNECTOR	= "KEY_CONNECTOR";

	private String		KEY_BROWSER	= "KEY_BROWSER";

	protected TCPConnector getClientConnector(CommandContext context) {
		return (TCPConnector) context.getAttribute(KEY_CONNECTOR);
	}

	protected void setClientConnector(CommandContext context, IOConnector connector) {
		context.setAttribute(KEY_CONNECTOR, connector);
	}

	protected MessageBrowser getMessageBrowser(CommandContext context) {
		return (MessageBrowser) context.getAttribute(KEY_BROWSER);
	}

	protected void setMessageBrowser(CommandContext context, MessageBrowser connector) {
		context.setAttribute(KEY_BROWSER, connector);
	}

}
