package com.gifisan.nio.service.impl;

import com.gifisan.nio.component.RESMessage;
import com.gifisan.nio.service.AbstractNIOFilter;
import com.gifisan.nio.service.Request;
import com.gifisan.nio.service.Response;

public class DeployFilter extends AbstractNIOFilter {

	public void accept(Request request, Response response) throws Exception {
		response.write(RESMessage.R_FAIL.toString());
	}
	
}
