package com.gifisan.nio.component.protocol;

import java.io.InputStream;

import com.gifisan.nio.component.Session;

public abstract class AbstractIOReadFuture extends AbstractReadFuture implements IOReadFuture {

	public AbstractIOReadFuture(Session session) {
		super(session);
	}
	
	protected boolean isBeatPacket;
	
	public void flush() {
		endPoint.incrementWriter();
		flushed = true;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setHasOutputStream(boolean hasOutputStream) {
		this.hasOutputStream = hasOutputStream;
	}
	
	public boolean isBeatPacket() {
		return isBeatPacket;
	}
}
