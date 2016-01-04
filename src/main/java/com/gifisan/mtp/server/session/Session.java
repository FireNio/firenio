package com.gifisan.mtp.server.session;

import com.gifisan.mtp.server.Attributes;
import com.gifisan.mtp.server.ServerEndPoint;
import com.gifisan.mtp.server.ServletContext;

public interface Session extends Attributes {

	public abstract void active(ServerEndPoint endPoint);

	public abstract ServletContext getServletContext();

	public abstract long getCreationTime();

	public abstract String getSessionID();

	public abstract long getLastAccessedTime();

	public abstract long getMaxInactiveInterval();

	public abstract boolean isValid();

	public abstract void setMaxInactiveInterval(long millisecond);
	
	public abstract boolean connecting();
	
	/**
	 * 获取connection的comment，默认值0
	 * @return
	 */
	public abstract int getComment();
	
	/**
	 * 设置connection的comment,默认值0
	 * @param state
	 */
	public abstract void setComment(int comment);
	
	/**
	 * 获取connection的附件
	 * 
	 * @return
	 */
	public abstract Object attachment() ;

	
	/**
	 * 设置connection的附件
	 * @param attachment
	 */
	public abstract void attach(Object attachment) ;

}
