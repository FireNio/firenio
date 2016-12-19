package com.generallycloud.nio.component;


public class SocketSessionFactoryImpl implements SocketSessionFactory {

	@Override
	public UnsafeSocketSession newUnsafeSession(SocketChannel channel) {

		return new UnsafeSocketSessionImpl(channel, channel.getChannelID());
	}

}
