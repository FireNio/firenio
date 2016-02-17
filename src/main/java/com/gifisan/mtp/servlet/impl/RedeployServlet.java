package com.gifisan.mtp.servlet.impl;

import com.gifisan.mtp.component.Configuration;
import com.gifisan.mtp.component.RESMessage;
import com.gifisan.mtp.component.RequestParam;
import com.gifisan.mtp.server.MTPServlet;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.ServerContext;

public class RedeployServlet extends MTPServlet {

	private String	username	= null;
	private String	password	= null;
	
	public void accept(Request request, Response response) throws Exception {
		RequestParam param = request.getParameters();
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
