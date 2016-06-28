package com.gifisan.nio.extend.plugin.jms.client.impl;

import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.connector.ClientStreamAcceptor;
import com.gifisan.nio.extend.FixedSession;

public class ConsumerStreamAcceptor implements ClientStreamAcceptor{

	public void accept(FixedSession session, ReadFuture future) throws Exception {
		future.setOutputStream(new BufferedOutputStream(future.getStreamLength()));
	}
	
}
