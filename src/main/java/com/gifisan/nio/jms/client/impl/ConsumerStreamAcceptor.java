package com.gifisan.nio.jms.client.impl;

import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.client.ClientStreamAcceptor;
import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.future.ReadFuture;

public class ConsumerStreamAcceptor implements ClientStreamAcceptor{

	public void accept(ClientSession session, ReadFuture future) throws Exception {
		future.setOutputIOEvent(new BufferedOutputStream(future.getStreamLength()),null);
	}
	
}
