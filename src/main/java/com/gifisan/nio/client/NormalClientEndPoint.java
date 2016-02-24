package com.gifisan.nio.client;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class NormalClientEndPoint extends ClientEndPointImpl implements ClientEndPoint{

	public NormalClientEndPoint(SocketChannel channel) {
		super(channel);
	}
	
	public void register(Selector selector,int option) throws ClosedChannelException{
		channel.register(selector, option);
	}
	
	
}
