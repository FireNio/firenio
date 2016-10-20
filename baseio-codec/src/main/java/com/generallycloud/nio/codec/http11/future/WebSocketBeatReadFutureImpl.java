package com.generallycloud.nio.codec.http11.future;

import com.generallycloud.nio.codec.http11.WebSocketProtocolDecoder;
import com.generallycloud.nio.component.BaseContext;

public class WebSocketBeatReadFutureImpl extends WebSocketReadFutureImpl implements WebSocketReadFuture{

	public WebSocketBeatReadFutureImpl(BaseContext context,boolean ping) {
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
