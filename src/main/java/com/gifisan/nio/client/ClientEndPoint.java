package com.gifisan.nio.client;

import java.net.SocketException;
import java.nio.channels.SelectionKey;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.NIOEndPoint;
import com.gifisan.nio.server.NIOContext;

public class ClientEndPoint extends NIOEndPoint {

	private ClientConnector		connector			= null;

	public ClientEndPoint(NIOContext context, SelectionKey selectionKey, ClientConnector connector)
			throws SocketException {
		super(context, selectionKey,connector.getEndPointWriter());
		this.connector = connector;
	}

	protected void extendClose() {
		
		new Thread(new Runnable() {
			
			public void run() {
				CloseUtil.close(connector);
			}
		}).start();
	}
}
