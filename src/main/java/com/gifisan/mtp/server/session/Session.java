package com.gifisan.mtp.server.session;

import com.gifisan.mtp.server.Attributes;
import com.gifisan.mtp.server.ServerEndPoint;
import com.gifisan.mtp.server.ServletContext;

public interface Session extends Attributes {
	
	public abstract boolean active(ServerEndPoint endPoint);

	public abstract void attach(Object attachment) ;

	public abstract Object attachment() ;

	public abstract boolean connecting();

	public abstract void destroy();

	/**
	 * 获取connection的comment，默认值0
	 * @return
	 */
	public abstract int getComment();

	public abstract long getCreationTime();

	public abstract long getLastAccessedTime();

	public abstract long getMaxInactiveInterval();
	
	public abstract ServletContext getServletContext();
	
	public abstract String getSessionID();
	
	public abstract boolean isValid();
	
	/**
	 * 设置connection的comment,默认值0
	 * @param state
	 */
	public abstract void setComment(int comment);
	
	public abstract void setEventListener(SessionEventListener listener);
	
	public abstract void setMaxInactiveInterval(long millisecond);

}
