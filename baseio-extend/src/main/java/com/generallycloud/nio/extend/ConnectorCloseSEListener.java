package com.generallycloud.nio.extend;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.component.SEListenerAdapter;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.connector.SocketChannelConnector;

public class ConnectorCloseSEListener extends SEListenerAdapter {

	private SocketChannelConnector	connector	= null;

	public ConnectorCloseSEListener(SocketChannelConnector connector) {
		this.connector = connector;
	}

	public void sessionClosed(Session session) {

		session.getEventLoop().dispatch(new Runnable() {
			public void run() {
				CloseUtil.close(connector);
			}
		});
	}

}
