package com.generallycloud.nio.codec.http11.future;

import com.generallycloud.nio.codec.http11.WebSocketProtocolDecoder;
import com.generallycloud.nio.component.NIOContext;

public class WebSocketBeatReadFutureImpl extends WebSocketReadFutureImpl implements WebSocketReadFuture{

	public WebSocketBeatReadFutureImpl(NIOContext context,boolean ping) {
		super(context);
		if (ping) {
			this.type = WebSocketProtocolDecoder.TYPE_PING;
			this.setPING();
		}else{
			this.type = WebSocketProtocolDecoder.TYPE_PONG;
			this.setPONG();
		}
	}
}
