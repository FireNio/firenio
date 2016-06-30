package com.gifisan.nio.extend;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.SessionEventListener;
import com.gifisan.nio.connector.TCPConnector;

public class ConnectorCloseSEListener implements SessionEventListener {

	private TCPConnector	connector	= null;

	public ConnectorCloseSEListener(TCPConnector connector) {
		this.connector = connector;
	}

	public void sessionOpened(Session session) {
	}

	public void sessionClosed(Session session) {
		
		session.getContext().getThreadPool().dispatch(new Runnable() {
			public void run() {
				CloseUtil.close(connector);
			}
		});
	}

}
