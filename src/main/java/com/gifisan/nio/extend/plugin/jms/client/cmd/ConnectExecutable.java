package com.gifisan.nio.extend.plugin.jms.client.cmd;

import java.util.HashMap;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.common.cmd.CmdResponse;
import com.gifisan.nio.common.cmd.CommandContext;
import com.gifisan.nio.component.DefaultNIOContext;
import com.gifisan.nio.component.LoggerSEtListener;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.connector.IOConnector;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.ConnectorCloseSEListener;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.SimpleIOEventHandle;
import com.gifisan.nio.extend.configuration.ServerConfiguration;
import com.gifisan.nio.extend.implementation.SYSTEMStopServerServlet;
import com.gifisan.nio.extend.plugin.jms.client.MessageBrowser;
import com.gifisan.nio.extend.plugin.jms.client.impl.DefaultMessageBrowser;

public class ConnectExecutable extends MQCommandExecutor {

	private Logger	logger		= LoggerFactory.getLogger(ConnectExecutable.class);
	
	public CmdResponse exec(CommandContext context, HashMap<String, String> params) {

		CmdResponse response = new CmdResponse();
		
		TCPConnector connector = getClientConnector(context);
		
		if (connector != null) {
			response.setResponse("已登录。");
			return response;
		}

		String username = params.get("-un");
		String password = params.get("-p");
		String host     = params.get("-host");
		String port     = params.get("-port");
		
		if (StringUtil.isNullOrBlank(username) 
				|| StringUtil.isNullOrBlank(password)
				|| StringUtil.isNullOrBlank(host)
				|| StringUtil.isNullOrBlank(port)) {
			response.setResponse("参数不正确！\n"
									+"example:\n"
									+"connect -host:localhost -port:8300 -un:admin -p:admin100");
			return response;
		}
		
		try {
			
			ServerConfiguration serverConfiguration = new ServerConfiguration();
			
			serverConfiguration.setSERVER_TCP_PORT(Integer.parseInt(port));

			String serviceName = SYSTEMStopServerServlet.SERVICE_NAME;

			SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

			NIOContext nioContext = new DefaultNIOContext();
			
			nioContext.setServerConfiguration(serverConfiguration);

			nioContext.setIOEventHandleAdaptor(eventHandle);

			nioContext.addSessionEventListener(new LoggerSEtListener());

			nioContext.addSessionEventListener(new ConnectorCloseSEListener(connector));

			connector.setContext(nioContext);

			FixedSession session = eventHandle.getFixedSession();
			
			//FIXME denglu cuowu 
			session.login(username, password);
			
			MessageBrowser browser = new DefaultMessageBrowser(session);
			
			response.setResponse("连接成功！");
			
			setClientConnector(context, connector);
			setMessageBrowser(context, browser);
			
		} catch (Exception e) {
			setClientConnector(context, null);
			setMessageBrowser(context, null);
			response.setResponse(e.getMessage());
			//debug
			logger.debug(e);
		}
		return response;
	}
}
