package com.gifisan.nio.server;

import com.gifisan.nio.Attachment;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.InputStream;
import com.gifisan.nio.server.session.InnerSession;

/**
 * <pre>
 * [0       ~              9]
 *  0       = 类型 [3=心跳，0=TEXT，1=STREAM，2=MULT]
 *  1       = session id
 *  2       = service name的长度
 *  3,4,5   = text content的长度
 *  6,7,8,9 = stream content的长度
 * </pre>
 * 
 */
public interface ServerEndPoint extends EndPoint {
	
	public abstract void endConnect();
	
	public abstract void attach(Attachment attachment);

	public abstract Attachment attachment();

	public abstract int getMark();
	
	public abstract ServerContext getContext() ;

	public abstract long getEndPointID();

	public abstract String getLocalAddr();

	public abstract String getLocalHost();

	public abstract int getLocalPort();

	public abstract int getMaxIdleTime();

	public abstract String getRemoteAddr();

	public abstract String getRemoteHost();

	public abstract int getRemotePort();

	public abstract InnerSession getSession(byte sessionID);

	public abstract boolean inStream();

	public abstract boolean isBlocking();

	public abstract boolean isEndConnect();

	public abstract boolean isOpened();

	public abstract int sessionSize();
	
	public abstract void setMark(int mark);

	public abstract void setInputStream(InputStream inputStream);

}
