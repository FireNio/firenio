package com.gifisan.nio.server;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.Attachment;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.server.session.InnerSession;


public interface ServerEndPoint extends EndPoint {
	
	public abstract void attach(Attachment attachment);

	public abstract Attachment attachment();

	public abstract int getMark();

	public abstract ServerContext getContext();

	public abstract long getEndPointID();

	public abstract InnerSession getSession(byte sessionID);

	public abstract void setMark(int mark);

	public abstract boolean inStream();
	
	public abstract boolean flushServerOutputStream(ByteBuffer buffer) throws IOException;
	
	public abstract void setStreamAvailable(int streamAvailable) ;

}
