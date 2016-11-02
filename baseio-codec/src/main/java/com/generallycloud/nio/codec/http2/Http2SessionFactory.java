package com.generallycloud.nio.codec.http2;

import com.generallycloud.nio.component.SessionFactory;
import com.generallycloud.nio.component.SocketChannel;
import com.generallycloud.nio.component.UnsafeSession;

public class Http2SessionFactory implements SessionFactory{

	public UnsafeSession newUnsafeSession(SocketChannel channel) {
		return new Http2SocketSessionImpl(channel, channel.getChannelID());
	}
}
