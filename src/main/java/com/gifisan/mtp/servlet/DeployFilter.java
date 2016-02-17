package com.gifisan.mtp.servlet;

import com.gifisan.mtp.component.RESMessage;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;

public class DeployFilter extends AbstractMTPFilter {

	public void accept(Request request, Response response) throws Exception {
		response.write(RESMessage.R_FAIL.toString());
	}
	
}
