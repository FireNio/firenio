package com.generallycloud.nio.component.protocol.http11.future;

import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.http11.WebSocketProtocolDecoder;

public class WebSocketBeatReadFutureImpl extends WebSocketReadFutureImpl implements WebSocketReadFuture{

	public WebSocketBeatReadFutureImpl(Session session) {
		super(session);
		this.type = WebSocketProtocolDecoder.TYPE_PING;
		this.isBeatPacket = true;
	}
	
}
