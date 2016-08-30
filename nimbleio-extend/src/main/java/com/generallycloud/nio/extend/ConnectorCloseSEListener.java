package com.generallycloud.nio.extend;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.component.SEListenerAdapter;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.connector.TCPConnector;

public class ConnectorCloseSEListener extends SEListenerAdapter {

	private TCPConnector	connector	= null;

	public ConnectorCloseSEListener(TCPConnector connector) {
		this.connector = connector;
	}

	public void sessionClosed(Session session) {

		session.getContext().getThreadPool().dispatch(new Runnable() {
			public void run() {
				CloseUtil.close(connector);
			}
		});
	}

}
