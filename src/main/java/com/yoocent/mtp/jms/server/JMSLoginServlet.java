package com.yoocent.mtp.jms.server;

import com.yoocent.mtp.component.ServletConfig;
import com.yoocent.mtp.server.MTPServlet;
import com.yoocent.mtp.server.Request;
import com.yoocent.mtp.server.Response;
import com.yoocent.mtp.server.context.ServletContext;
import com.yoocent.mtp.server.session.Session;

public class JMSLoginServlet extends MTPServlet{

	public static String SERVICE_NAME = JMSLoginServlet.class.getSimpleName();
	
	private static final byte TRUE = 'T';
	
	private static final byte FALSE = 'F';
	
	
	public void accept(Request request, Response response) throws Exception {
		
		
		String username = request.getStringParameter("username");
		
		String password = request.getStringParameter("password");
		
		boolean result = this.username.equals(username) && this.password.equals(password);
		if (result) {
			
			MQContext context = MQContextFactory.getMQContext();
			Session session = request.getSession();
			context.setLogined(true,session);
//			session.setAttribute("login", true);
//			session.setAttribute("username", username);
			
			response.write(TRUE);
		}else{
			
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
		
		super.initialize(context, config);
	}
	
	

}
