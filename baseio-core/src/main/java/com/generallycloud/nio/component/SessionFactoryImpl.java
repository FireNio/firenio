package com.generallycloud.nio.component;

import com.generallycloud.nio.component.concurrent.EventLoopGroup;

public class SessionFactoryImpl implements SessionFactory {

	private BaseContext	context;

	public SessionFactoryImpl(BaseContext context) {
		this.context = context;
	}

	public UnsafeSession newUnsafeSession(SocketChannel channel) {

		EventLoopGroup eventLoopGroup = context.getEventLoopGroup();

		if (eventLoopGroup == null) {
			return new UnsafeSessionImpl(channel, null, channel.getChannelID());
		}

		return new UnsafeSessionImpl(channel, eventLoopGroup.getNext(), channel.getChannelID());
	}

}
