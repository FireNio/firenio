package com.generallycloud.nio.codec.http11.future;

import com.generallycloud.nio.codec.http11.WebSocketProtocolDecoder;
import com.generallycloud.nio.component.BaseContext;

public class WebSocketTextReadFutureImpl extends WebSocketReadFutureImpl implements WebSocketReadFuture{

	public WebSocketTextReadFutureImpl(BaseContext context) {
		super(context);
		this.type = WebSocketProtocolDecoder.TYPE_TEXT;
	}
	
}
