package com.gifisan.mtp.server;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.mtp.component.MTPRequestInputStream;
import com.gifisan.mtp.component.ProtocolDecoder;

public interface ServerEndPoint extends EndPoint{

	public abstract void attach(Object attachment);
	
	public abstract Object attachment();
	
	/**
	 * get state,default value 0
	 * @return
	 */
	public abstract int comment();
	
	public abstract ByteBuffer completeRead(int limit) throws IOException;
	
    public abstract void endConnect();
	
	public abstract MTPRequestInputStream getInputStream();
	
	public abstract String getLocalAddr();
	
	public abstract String getLocalHost();
	
	public abstract int getLocalPort();
	
	public abstract int getMaxIdleTime();
	
	public abstract ProtocolDecoder getProtocolDecoder();

	public abstract String getRemoteAddr();
	
	public abstract String getRemoteHost();
	
	public abstract int getRemotePort();
	
	public abstract boolean inStream();
	
	public abstract boolean isBlocking();
	
	public abstract boolean isEndConnect();
	
	public abstract boolean isOpened();
	
	public abstract boolean protocolDecode(ServletContext context) throws IOException;
	
	public abstract ByteBuffer read(int limit) throws IOException;
	
	/**
     * <pre>
	 * [0       ~              9]
	 *  0       = 类型 [0=心跳，1=TEXT，2=STREAM，3=MULT]
	 *  1       = session id的长度
	 *  2       = service name的长度
	 *  3,4,5   = parameters的长度
	 *  6,7,8,9 = 文件的长度
     * </pre>
     * @return int
     * @throws IOException
     */
	public abstract int readHead(ByteBuffer buffer) throws IOException;
	
	/**
	 * set state,default value 0
	 * @param state
	 */
	public abstract void setComment(int comment);
	
	public abstract void setMTPRequestInputStream(MTPRequestInputStream inputStream);
	
}
