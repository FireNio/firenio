package com.gifisan.nio.server.service.impl;

import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.RESMessage;
import com.gifisan.nio.server.service.AbstractNIOFilter;

public class DeployFilter extends AbstractNIOFilter {

	public void accept(IOSession session,ServerReadFuture future) throws Exception {
		future.write(RESMessage.R_FAIL.toString());
		session.flush(future);
	}
	
}
