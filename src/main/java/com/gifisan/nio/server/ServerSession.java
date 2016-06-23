package com.gifisan.nio.server;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.AbstractSession;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.UDPEndPoint;

public class ServerSession extends AbstractSession implements Session {

	private NIOContext			context			= null;

	private static final Logger	logger			= LoggerFactory.getLogger(ServerSession.class);

	public ServerSession(TCPEndPoint endPoint) {
		super(endPoint);

		this.context = (NIOContext) endPoint.getContext();
	}

	public void destroy() {



		super.destroy();
	}


}
