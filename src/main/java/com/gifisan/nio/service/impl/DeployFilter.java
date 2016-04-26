package com.gifisan.nio.service.impl;

import com.gifisan.nio.component.ReadFuture;
import com.gifisan.nio.server.RESMessage;
import com.gifisan.nio.server.session.IOSession;
import com.gifisan.nio.service.AbstractNIOFilter;

public class DeployFilter extends AbstractNIOFilter {

	public void accept(IOSession session,ReadFuture future) throws Exception {
		session.write(RESMessage.R_FAIL.toString());
		session.flush(future);
	}
	
}
