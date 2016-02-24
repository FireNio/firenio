package com.gifisan.nio.server.session;

import com.gifisan.nio.server.Attributes;
import com.gifisan.nio.server.ServerContext;

public interface Session extends Attributes {
	
	public abstract void attach(Object attachment) ;

	public abstract Object attachment() ;

	/**
	 * 断开此EndPoint
	 */
	public abstract void disconnect();

	public abstract int getEndpointMark();

	public abstract long getCreationTime();

	public abstract long getLastAccessedTime();

	public abstract ServerContext getServerContext();
	
	public abstract void setEndpointMark(int mark);
	
	public abstract void addEventListener(SessionEventListener listener);
	
}
