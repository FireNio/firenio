package com.gifisan.nio.server.service.impl;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.server.RESMessage;
import com.gifisan.nio.server.service.AbstractNIOFilter;

public class DeployFilter extends AbstractNIOFilter {

	public void accept(Session session,ReadFuture future) throws Exception {
		future.write(RESMessage.SYSTEM_ERROR.toString());
		session.flush(future);
	}
	
}
