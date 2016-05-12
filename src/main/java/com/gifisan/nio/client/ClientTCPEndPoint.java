package com.gifisan.nio.client;

import java.net.SocketException;
import java.nio.channels.SelectionKey;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.DefaultTCPEndPoint;
import com.gifisan.nio.server.NIOContext;

public class ClientTCPEndPoint extends DefaultTCPEndPoint {

	private ClientTCPConnector	connector	= null;

	public ClientTCPEndPoint(NIOContext context, SelectionKey selectionKey, ClientTCPConnector connector)
			throws SocketException {
		super(context, selectionKey, connector.getEndPointWriter());
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
