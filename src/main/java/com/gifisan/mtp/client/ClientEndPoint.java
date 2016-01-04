package com.gifisan.mtp.client;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;

import com.gifisan.mtp.server.EndPoint;

public interface ClientEndPoint extends EndPoint{
	
	public abstract void register(Selector selector,int option) throws ClosedChannelException;
}
