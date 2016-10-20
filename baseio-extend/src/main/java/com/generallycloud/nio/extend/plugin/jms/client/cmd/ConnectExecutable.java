package com.generallycloud.nio.extend.plugin.jms.client.cmd;

import java.util.HashMap;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.common.cmd.CmdResponse;
import com.generallycloud.nio.common.cmd.CommandContext;
import com.generallycloud.nio.component.BaseContextImpl;
import com.generallycloud.nio.component.LoggerSEListener;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.ConnectorCloseSEListener;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.SimpleIOEventHandle;
import com.generallycloud.nio.extend.plugin.jms.client.MessageBrowser;
import com.generallycloud.nio.extend.plugin.jms.client.impl.DefaultMessageBrowser;

@Deprecated
public class ConnectExecutable extends MQCommandExecutor {

	private Logger	logger		= LoggerFactory.getLogger(ConnectExecutable.class);
	
	public CmdResponse exec(CommandContext context, HashMap<String, String> params) {

		CmdResponse response = new CmdResponse();
		
		SocketChannelConnector connector = getClientConnector(context);
		
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
			
			ServerConfiguration configuration = new ServerConfiguration();
			
			configuration.setSERVER_TCP_PORT(Integer.parseInt(port));
			
			connector = new SocketChannelConnector();

//			String serviceName = SYSTEMStopServerServlet.SERVICE_NAME;

			SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

			BaseContext baseContext = new BaseContextImpl(configuration);
			
			baseContext.setIOEventHandleAdaptor(eventHandle);

			baseContext.addSessionEventListener(new LoggerSEListener());

			baseContext.addSessionEventListener(new ConnectorCloseSEListener(connector));

			connector.setContext(baseContext);

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
