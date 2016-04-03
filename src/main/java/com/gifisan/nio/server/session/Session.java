package com.gifisan.nio.server.session;

import java.io.OutputStream;

import com.gifisan.nio.Attachment;
import com.gifisan.nio.concurrent.ExecutorThreadPool;
import com.gifisan.nio.server.Attributes;
import com.gifisan.nio.server.ServerContext;

public interface Session extends Attributes {
	
	public abstract void attach(Attachment attachment) ;

	public abstract Attachment attachment() ;

	/**
	 * 断开此EndPoint
	 */
	public abstract void disconnect();

	public abstract int getEndpointMark();

	public abstract long getCreationTime();

	public abstract ServerContext getServerContext();
	
	public abstract void setEndpointMark(int mark);
	
	public abstract void addEventListener(SessionEventListener listener);
	
	public abstract OutputStream getServerOutputStream() ;

	public abstract void setServerOutputStream(OutputStream serverOutputStream) ;
	
	public abstract ExecutorThreadPool getExecutorThreadPool();
	
	public abstract boolean isStream();
	
	public abstract void setStream(boolean stream);
}
