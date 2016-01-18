package com.gifisan.mtp.jms.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.component.RequestParam;
import com.gifisan.mtp.component.ServletConfig;
import com.gifisan.mtp.server.MTPServlet;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.ServletContext;
import com.gifisan.mtp.server.session.Session;

public class JMSLoginServlet extends MTPServlet {

	private byte			FALSE		= 'F';
	private Logger			logger		= LoggerFactory.getLogger(JMSLoginServlet.class);
	private String			password		= null;
	private byte			TRUE			= 'T';
	private String			username		= null;

	public void accept(Request request, Response response) throws Exception {

		RequestParam param = request.getParameters();

		String username = param.getParameter("username");

		String password = param.getParameter("password");

		boolean result = this.username.equals(username) && this.password.equals(password);
		if (result) {

			MQContext context = MQContextFactory.getMQContext();
			Session session = request.getSession();
			session.addEventListener(new TransactionProtectListener());
			context.setLogined(true, session);
			// session.setAttribute("login", true);
			// session.setAttribute("username", username);
			logger.info("user [" + username + "] login successful!");
			response.write(TRUE);
		} else {
			request.getSession().destroy();
			logger.info("user [" + username + "] login failed!");
			response.write(FALSE);

		}
		response.flush();

	}

	public void destroy(ServletContext context, ServletConfig config) throws Exception {

		MQContext mqContext = MQContextFactory.getMQContext();

		LifeCycleUtil.stop(mqContext);

		super.destroy(context, config);
	}

	public void initialize(ServletContext context, ServletConfig config) throws Exception {

		// TODO read from file or others
		// this.username = "admin";
		// this.password = "admin100";

		this.username = config.getStringValue("username");
		this.password = config.getStringValue("password");

		MQContext mqContext = MQContextFactory.getMQContext();

		String dueTimeValue = config.getStringValue("due-time");

		long dueTime = 0L;

		if (StringUtil.isNullOrBlank(dueTimeValue)) {
			dueTime = 1000 * 60 * 60 * 24 * 7;
		} else {
			dueTime = Long.valueOf(dueTimeValue);
		}

		mqContext.setMessageDueTime(dueTime);
		mqContext.start();

		super.initialize(context, config);
	}

}
