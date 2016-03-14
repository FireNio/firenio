package com.gifisan.nio.servlet.impl;

import com.gifisan.nio.component.RESMessage;
import com.gifisan.nio.server.Request;
import com.gifisan.nio.server.Response;
import com.gifisan.nio.servlet.AbstractNIOFilter;

public class DeployFilter extends AbstractNIOFilter {

	public void accept(Request request, Response response) throws Exception {
		response.write(RESMessage.R_FAIL.toString());
	}
	
}
