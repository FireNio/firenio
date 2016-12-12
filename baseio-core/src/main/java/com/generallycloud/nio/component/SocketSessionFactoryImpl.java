package com.generallycloud.nio.component;


public class SocketSessionFactoryImpl implements SocketSessionFactory {

	public UnsafeSocketSession newUnsafeSession(SocketChannel channel) {

		return new UnsafeSocketSessionImpl(channel, channel.getChannelID());
	}

}
