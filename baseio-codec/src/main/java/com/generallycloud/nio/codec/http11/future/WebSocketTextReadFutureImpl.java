package com.generallycloud.nio.codec.http11.future;

import com.generallycloud.nio.codec.http11.WebSocketProtocolDecoder;
import com.generallycloud.nio.component.SocketChannelContext;

public class WebSocketTextReadFutureImpl extends WebSocketReadFutureImpl{

	public WebSocketTextReadFutureImpl(SocketChannelContext context) {
		super(context);
		this.type = WebSocketProtocolDecoder.TYPE_TEXT;
	}
	
}
