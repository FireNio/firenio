package com.gifisan.nio.jms.client.cmd;

import com.gifisan.nio.client.ClientConnector;
import com.gifisan.nio.common.cmd.CommandContext;
import com.gifisan.nio.common.cmd.Executable;
import com.gifisan.nio.jms.client.MessageBrowser;

public abstract class JMSCommandExecutor implements Executable {

	private String		KEY_CONNECTOR	= "KEY_CONNECTOR";

	private String		KEY_BROWSER	= "KEY_BROWSER";

	protected ClientConnector getClientConnector(CommandContext context) {
		return (ClientConnector) context.getAttribute(KEY_CONNECTOR);
	}

	protected void setClientConnector(CommandContext context, ClientConnector connector) {
		context.setAttribute(KEY_CONNECTOR, connector);
	}

	protected MessageBrowser getMessageBrowser(CommandContext context) {
		return (MessageBrowser) context.getAttribute(KEY_BROWSER);
	}

	protected void setMessageBrowser(CommandContext context, MessageBrowser connector) {
		context.setAttribute(KEY_BROWSER, connector);
	}

}
