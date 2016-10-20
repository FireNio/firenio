package com.generallycloud.nio.codec.http11.future;

import com.generallycloud.nio.codec.http11.WebSocketProtocolDecoder;
import com.generallycloud.nio.component.NIOContext;

public class WebSocketTextReadFutureImpl extends WebSocketReadFutureImpl implements WebSocketReadFuture{

	public WebSocketTextReadFutureImpl(NIOContext context) {
		super(context);
		this.type = WebSocketProtocolDecoder.TYPE_TEXT;
	}
	
}
