package com.generallycloud.nio.codec.http11.future;

import com.generallycloud.nio.codec.http11.WebSocketProtocolFactory;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.SocketSEListenerAdapter;
import com.generallycloud.nio.component.SocketSession;

public class WebSocketSEListener extends SocketSEListenerAdapter{
	
	private Logger logger = LoggerFactory.getLogger(WebSocketSEListener.class);

	public void sessionClosed(SocketSession session) {
		
		if(!WebSocketProtocolFactory.PROTOCOL_ID.equals(session.getProtocolID())){
			return;
		}
		
		WebSocketReadFuture future = new WebSocketReadFutureImpl(session);
		
		try {
			session.getContext().getForeReadFutureAcceptor().accept(session, future);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		
		super.sessionClosed(session);
	}
	
}
