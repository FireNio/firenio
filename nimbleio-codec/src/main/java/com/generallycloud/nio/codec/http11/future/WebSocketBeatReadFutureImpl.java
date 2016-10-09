package com.generallycloud.nio.codec.http11.future;

import com.generallycloud.nio.codec.http11.WebSocketProtocolDecoder;

public class WebSocketBeatReadFutureImpl extends WebSocketReadFutureImpl implements WebSocketReadFuture{

	public WebSocketBeatReadFutureImpl(boolean ping) {
		if (ping) {
			this.type = WebSocketProtocolDecoder.TYPE_PING;
			this.setPING();
		}else{
			this.type = WebSocketProtocolDecoder.TYPE_PONG;
			this.setPONG();
		}
	}
}
