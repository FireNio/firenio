package com.gifisan.mtp.server;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.mtp.component.EndPoint;
import com.gifisan.mtp.component.InputStream;
import com.gifisan.mtp.component.ProtocolDecoder;
import com.gifisan.mtp.server.session.InnerSession;

/**
 * <pre>
 * [0       ~              10]
 *  0       = 类型 [0=心跳，1=TEXT，2=STREAM，3=MULT]
 *  1       = session id
 *  2       = service name的长度
 *  3,4,5   = parameters的长度
 *  6,7,8,9 = 文件的长度
 * </pre>
 * 
 */
public interface ServerEndPoint extends EndPoint {
	
	
	public abstract void attach(Object attachment);

	public abstract Object attachment();

	/**
	 * get comment,default value 0
	 * 
	 * @return
	 */
	public abstract int getMark();
	
	public abstract void endConnect();

	public abstract ServerContext getContext() ;

	public abstract long getEndPointID();

	public abstract InputStream getInputStream();

	public abstract String getLocalAddr();

	public abstract String getLocalHost();

	public abstract int getLocalPort();

	public abstract int getMaxIdleTime();

	public abstract ProtocolDecoder getProtocolDecoder();

	public abstract String getRemoteAddr();

	public abstract String getRemoteHost();

	public abstract int getRemotePort();

	public abstract InnerSession getSession();

	public abstract boolean inStream();

	public abstract boolean isBlocking();

	public abstract boolean isEndConnect();

	public abstract boolean isOpened();

	public abstract boolean protocolDecode(ServerContext context) throws IOException;

	public abstract ByteBuffer read(int limit) throws IOException;

	public abstract int sessionSize();
	
	/**
	 * set state,default value 0
	 * 
	 * @param comment
	 */
	public abstract void setMark(int mark);

	public abstract void setInputStream(InputStream inputStream);

}
