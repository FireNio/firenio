package com.generallycloud.nio.component.protocol;

import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.SocketChannel;

public abstract class AbstractIOReadFuture extends AbstractReadFuture implements IOReadFuture {

	protected boolean	isHeartbeat;

	protected boolean	isPING;

	protected boolean	isPONG;

	protected AbstractIOReadFuture() {
	
	}

	public AbstractIOReadFuture(Session session) {
		this.update((IOSession) session);
	}
	
	public void update(IOSession session) {
		if (this.session == null) {
			this.session = session;
			this.channel = this.session.getSocketChannel();
		}
	}

	public void flush() {
		flushed = true;
	}

	public SocketChannel getSocketChannel() {
		return channel;
	}

	public boolean isHeartbeat() {
		return isHeartbeat;
	}

	public boolean isPING() {
		return isHeartbeat && isPING;
	}

	public boolean isPONG() {
		return isHeartbeat && isPONG;
	}

	public IOReadFuture setPING() {
		this.isPING = true;
		this.isHeartbeat = true;
		return this;
	}

	public IOReadFuture setPONG() {
		this.isPONG = true;
		this.isHeartbeat = true;
		return this;
	}
}
