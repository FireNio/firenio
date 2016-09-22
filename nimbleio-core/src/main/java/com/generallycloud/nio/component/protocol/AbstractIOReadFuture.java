package com.generallycloud.nio.component.protocol;

import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.SocketChannel;

public abstract class AbstractIOReadFuture extends AbstractReadFuture implements IOReadFuture {

	public AbstractIOReadFuture(Session session) {
		super(session);
	}

	protected boolean	isBeatPacket;

	public void flush() {
		flushed = true;
	}

	public boolean isBeatPacket() {
		return isBeatPacket;
	}

	public SocketChannel getSocketChannel() {
		return channel;
	}

}
