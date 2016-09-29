package com.generallycloud.nio.component.protocol.http11.future;

import com.generallycloud.nio.component.protocol.http11.WebSocketProtocolDecoder;

public class WebSocketTextReadFutureImpl extends WebSocketReadFutureImpl implements WebSocketReadFuture{

	public WebSocketTextReadFutureImpl() {
		this.type = WebSocketProtocolDecoder.TYPE_TEXT;
	}
	
}
