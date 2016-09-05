package com.generallycloud.nio.component.protocol.http11.future;

import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.http11.WebSocketProtocolDecoder;

public class WebSocketTextReadFutureImpl extends WebSocketReadFutureImpl implements WebSocketReadFuture{

	public WebSocketTextReadFutureImpl(Session session) {
		super(session);
		this.type = WebSocketProtocolDecoder.TYPE_TEXT;
	}
	
}
