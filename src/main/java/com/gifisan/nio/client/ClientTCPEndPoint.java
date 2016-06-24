package com.gifisan.nio.client;

import java.net.SocketException;
import java.nio.channels.SelectionKey;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.AbstractTCPEndPoint;
import com.gifisan.nio.server.NIOContext;

public class ClientTCPEndPoint extends AbstractTCPEndPoint {

	private TCPConnector			connector	= null;

	public ClientTCPEndPoint(NIOContext context, SelectionKey selectionKey, TCPConnector connector)
			throws SocketException {
		super(context, selectionKey, connector.getEndPointWriter());
		this.connector = connector;
	}

	protected void extendClose() {

		//FIXME soft close
		new Thread(new Runnable() {

			public void run() {
				CloseUtil.close(connector);
			}
		}).start();
	}

}
