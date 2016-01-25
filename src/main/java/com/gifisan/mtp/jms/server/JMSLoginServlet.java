package com.gifisan.mtp.jms.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.component.RequestParam;
import com.gifisan.mtp.component.ServletConfig;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.ServerContext;
import com.gifisan.mtp.server.session.Session;

public class JMSLoginServlet extends JMSServlet {

	private byte			FALSE		= 'F';
	private Logger			logger		= LoggerFactory.getLogger(JMSLoginServlet.class);
	private String			password		= null;
	private byte			TRUE			= 'T';
	private String			username		= null;

	public void accept(Request request, Response response) throws Exception {

		RequestParam param = request.getParameters();

		String _username = param.getParameter("username");

		String _password = param.getParameter("password");

		if (username.equals(_username) && password.equals(_password)) {

			MQContext context = getMQContext();
			Session session = request.getSession();
			
			boolean isConsumer = param.getBooleanParameter("consumer");
			
			if (isConsumer) {
				session.addEventListener(new TransactionProtectListener(context));
			}
			
			context.setLogined(true, session);
			logger.info("user [" + username + "] login successful!");
			response.write(TRUE);
		} else {
			request.getSession().disconnect();
			logger.info("user [" + username + "] login failed!");
			response.write(FALSE);

		}
		response.flush();

	}

	public void destroy(ServerContext context, ServletConfig config) throws Exception {

		MQContextFactory.setNullMQContext();
		
		super.destroy(context, config);
	}

	public void initialize(ServerContext context, ServletConfig config) throws Exception {

		// TODO read from file or others
		// this.username = "admin";
		// this.password = "admin100";

		this.username = config.getStringValue("username");
		this.password = config.getStringValue("password");

		MQContext mqContext = getMQContext();

		String dueTimeValue = config.getStringValue("due-time");

		long dueTime = 0L;

		if (StringUtil.isNullOrBlank(dueTimeValue)) {
			dueTime = 1000 * 60 * 60 * 24 * 7;
		} else {
			dueTime = Long.valueOf(dueTimeValue);
		}

		mqContext.setMessageDueTime(dueTime);
		
		MQContextFactory.initializeContext();
		
		super.initialize(context, config);
	}

}
