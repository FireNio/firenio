package com.gifisan.nio.service.impl;

import com.gifisan.nio.component.RESMessage;
import com.gifisan.nio.server.session.Session;
import com.gifisan.nio.service.AbstractNIOFilter;

public class DeployFilter extends AbstractNIOFilter {

	public void accept(Session session) throws Exception {
		session.write(RESMessage.R_FAIL.toString());
	}
	
}
