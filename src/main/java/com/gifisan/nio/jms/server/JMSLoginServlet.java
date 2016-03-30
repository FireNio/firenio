package com.gifisan.nio.jms.server;

import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.server.Request;
import com.gifisan.nio.server.Response;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.server.session.Session;

public class JMSLoginServlet extends JMSServlet {

	private byte			FALSE		= 'F';
	private String			password		= null;
	private byte			TRUE			= 'T';
	private String			username		= null;

	public void accept(Request request, Response response,JMSSessionAttachment attachment) throws Exception {

		Parameters param = request.getParameters();

		String _username = param.getParameter("username");

		String _password = param.getParameter("password");

		if (username.equals(_username) && password.equals(_password)) {
			
			MQContext context = getMQContext();
			
			Session session = request.getSession();
			
			if (attachment == null) {
				session.attach(new JMSSessionAttachment());
			}
			
			boolean isConsumer = param.getBooleanParameter("consumer");
			
			if (isConsumer) {
				session.addEventListener(new TransactionProtectListener());
			}

			context.setLogined(true, session);
			
			DebugUtil.debug("user [" + username + "] login successful!");
			
			response.write(TRUE);
		} else {
			request.getSession().disconnect();
			DebugUtil.debug("user [" + username + "] login failed!");
			response.write(FALSE);

		}
		response.flush();

	}
	
	public void prepare(ServerContext context, Configuration config) throws Exception {
		this.username = config.getProperty("username");
		this.password = config.getProperty("password");

		MQContext mqContext = getMQContext();

		long dueTime = config.getLongProperty("due-time");

		mqContext.setMessageDueTime(dueTime == 0 ? 1000 * 60 * 60 * 24 * 7 : dueTime);
	}

	public void unload(ServerContext context, Configuration config) throws Exception {
		
	}

	public void destroy(ServerContext context, Configuration config) throws Exception {

		MQContextFactory.setNullMQContext();
		
		super.destroy(context, config);
	}

	public void initialize(ServerContext context, Configuration config) throws Exception {

		this.username = config.getProperty("username");
		this.password = config.getProperty("password");

		MQContext mqContext = getMQContext();

		long dueTime = config.getLongProperty("due-time");

		mqContext.setMessageDueTime(dueTime == 0 ? 1000 * 60 * 60 * 24 * 7 : dueTime);
		
		MQContextFactory.initializeContext();
		
		super.initialize(context, config);
	}

}
