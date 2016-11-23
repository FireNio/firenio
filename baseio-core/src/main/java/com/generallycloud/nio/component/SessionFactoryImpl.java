package com.generallycloud.nio.component;


public class SessionFactoryImpl implements SessionFactory {

	public UnsafeSession newUnsafeSession(SocketChannel channel) {

		return new UnsafeSessionImpl(channel, channel.getChannelID());
	}

}
