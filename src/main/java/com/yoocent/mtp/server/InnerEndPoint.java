package com.yoocent.mtp.server;

import java.io.IOException;

import com.yoocent.mtp.component.MTPParser;
import com.yoocent.mtp.component.MTPRequestInputStream;

public interface InnerEndPoint extends EndPoint{

	public abstract MTPRequestInputStream getInputStream();
	
	public abstract void setMTPRequestInputStream(MTPRequestInputStream inputStream);
	
	public abstract MTPParser getParser() ;
	
	public abstract MTPParser genParser();
	
    /**
     * <pre>
	 * [0       ~              11]
	 *  0       = 类型
	 *  1       = 请求id的长度
	 *  2       = key的长度
	 *  3       = 保留位
	 *  4       = 保留位
	 *  5,6,7   = parameters的长度
	 *  8,9,10,11 = 文件的长度
     * </pre>
     * @throws IOException
     */
	public abstract byte[] readHead() throws IOException;
	
	public abstract boolean inStream();
	
	/**
	 * get state,default value 0
	 * @return
	 */
	public abstract int comment();
	
	/**
	 * set state,default value 0
	 * @param state
	 */
	public abstract void setComment(int comment);
	
	public abstract Object attachment();
	
	public abstract void attach(Object attachment);
	
}
