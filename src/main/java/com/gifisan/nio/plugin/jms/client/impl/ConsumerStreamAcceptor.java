package com.gifisan.nio.plugin.jms.client.impl;

import com.gifisan.nio.client.ConnectorSession;
import com.gifisan.nio.client.ClientStreamAcceptor;
import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.future.ReadFuture;

public class ConsumerStreamAcceptor implements ClientStreamAcceptor{

	public void accept(ConnectorSession session, ReadFuture future) throws Exception {
		future.setOutputStream(new BufferedOutputStream(future.getStreamLength()));
	}
	
}
