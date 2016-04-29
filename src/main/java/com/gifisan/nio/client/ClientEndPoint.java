package com.gifisan.nio.client;

import java.net.SocketException;
import java.nio.channels.SelectionKey;

import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.component.NIOEndPoint;
import com.gifisan.nio.component.SelectorManagerLoop;
import com.gifisan.nio.server.NIOContext;

public class ClientEndPoint extends NIOEndPoint{
	
	private SelectorManagerLoop selectorManagerLoop = null;

	public ClientEndPoint(NIOContext context, SelectionKey selectionKey,SelectorManagerLoop selectorManagerLoop) throws SocketException {
		super(context, selectionKey);
		this.selectorManagerLoop = selectorManagerLoop;
	}

	protected void extendClose(NIOContext context) {
		
		LifeCycleUtil.stop(selectorManagerLoop);
	}
	
}
