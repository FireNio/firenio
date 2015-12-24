package com.gifisan.mtp.jms.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.component.ServletConfig;
import com.gifisan.mtp.server.MTPServlet;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.context.ServletContext;
import com.gifisan.mtp.server.session.Session;

public class JMSLoginServlet extends MTPServlet{
	
	private final Logger logger = LoggerFactory.getLogger(JMSLoginServlet.class);

	public static String SERVICE_NAME = JMSLoginServlet.class.getSimpleName();
	
	private final byte TRUE = 'T';
	
	private final byte FALSE = 'F';	
	
	public void accept(Request request, Response response) throws Exception {
		
		
		String username = request.getParameter("username");
		
		String password = request.getParameter("password");
		
		boolean result = this.username.equals(username) && this.password.equals(password);
		if (result) {
			
			MQContext context = MQContextFactory.getMQContext();
			Session session = request.getSession();
			context.setLogined(true,session);
//			session.setAttribute("login", true);
//			session.setAttribute("username", username);
			logger.info("user ["+username+"] login successful!");
			response.write(TRUE);
		}else{
			logger.info("user ["+username+"] login failed!");
			response.write(FALSE);
		}
		response.flush();
		
	}
	
	private String username = null;
	
	private String password = null;

	public void initialize(ServletContext context, ServletConfig config)
			throws Exception {
		
		//TODO read from file or others
//		this.username = "admin";
//		this.password = "admin100";
		
		this.username = config.getStringValue("username");
		this.password = config.getStringValue("password");
		
		MQContext mqContext = MQContextFactory.getMQContext();
		
		
		String dueTimeValue = config.getStringValue("due-time");
		
		long dueTime = 0L;
		
		if (StringUtil.isNullOrBlank(dueTimeValue)) {
			dueTime = 1000 * 60 * 60 * 24 * 7;
		}else{
			dueTime = Long.valueOf(dueTimeValue);
		}
		
		mqContext.setMessageDueTime(dueTime);
		mqContext.start();
		
		super.initialize(context, config);
	}

	public void destroy(ServletContext context, ServletConfig config)
			throws Exception {
		
		MQContext mqContext = MQContextFactory.getMQContext();
		
		LifeCycleUtil.stop(mqContext);
		
		super.destroy(context, config);
	}
	
	

}
