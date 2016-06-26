package com.gifisan.nio.plugin.jms.client.cmd;

import com.gifisan.nio.common.cmd.CommandContext;
import com.gifisan.nio.common.cmd.Executable;
import com.gifisan.nio.connector.IOConnector;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.plugin.jms.client.MessageBrowser;

public abstract class JMSCommandExecutor implements Executable {

	private String		KEY_CONNECTOR	= "KEY_CONNECTOR";

	private String		KEY_BROWSER	= "KEY_BROWSER";

	protected IOConnector getClientConnector(CommandContext context) {
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
