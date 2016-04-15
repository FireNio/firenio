package com.gifisan.nio.service.impl;

import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.RESMessage;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.service.NIOServlet;
import com.gifisan.nio.service.Request;
import com.gifisan.nio.service.Response;

public class RedeployServlet extends NIOServlet {

	private String	username	= null;
	private String	password	= null;
	
	public void accept(Request request, Response response) throws Exception {
		Parameters param = request.getParameters();
		String username = param.getParameter("username");
		String password = param.getParameter("password");

		boolean result = this.username.equals(username) && this.password.equals(password);
		if (result) {
			ServerContext context = request.getSession().getServerContext();
			RESMessage message =  context.redeploy()  ? RESMessage.R_SUCCESS : RESMessage.R_FAIL;
			response.write(message.toString());
		} else {
			response.write(RESMessage.R_UNAUTH.toString());
		}
		response.flush();

	}

	public void initialize(ServerContext context, Configuration config) throws Exception {
		this.username = config.getProperty("username");
		this.password = config.getProperty("password");
	}

}
