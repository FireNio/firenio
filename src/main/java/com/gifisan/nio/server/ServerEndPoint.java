package com.gifisan.nio.server;

import com.gifisan.nio.Attachment;
import com.gifisan.nio.component.EndPoint;
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

	public abstract void attach(Attachment attachment);

	public abstract Attachment attachment();

	public abstract int getMark();

	public abstract ServerContext getContext();

	public abstract long getEndPointID();

	public abstract InnerSession getSession(byte sessionID);

	public abstract boolean isEndConnect();

	public abstract void setMark(int mark);

}
