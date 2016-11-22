package com.generallycloud.nio.codec.http2;

import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.SessionFactory;
import com.generallycloud.nio.component.SocketChannel;
import com.generallycloud.nio.component.UnsafeSession;
import com.generallycloud.nio.component.concurrent.EventLoopGroup;

public class Http2SessionFactory implements SessionFactory {

	private BaseContext context;
	
	public Http2SessionFactory(BaseContext context) {
		this.context = context;
	}

	public UnsafeSession newUnsafeSession(SocketChannel channel) {
		
		EventLoopGroup	eventLoopGroup = context.getEventLoopGroup();

		if (eventLoopGroup == null) {
			return new Http2SocketSessionImpl(channel, null, channel.getChannelID());
		}

		return new Http2SocketSessionImpl(channel, eventLoopGroup.getNext(), channel.getChannelID());
	}
}
