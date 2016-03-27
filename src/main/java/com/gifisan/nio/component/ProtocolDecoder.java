package com.gifisan.nio.component;

import java.io.IOException;

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
public interface ProtocolDecoder {

	public static final byte	BEAT		= 3;
	public static final byte	MULT		= 2;
	public static final byte	STREAM	= 1;
	public static final byte	TEXT		= 0;

	public abstract boolean decode(EndPoint endPoint, ProtocolDataImpl data) throws IOException;

	public abstract boolean doDecodeExtend(EndPoint endPoint, ProtocolDataImpl data, byte type) throws IOException;

	public abstract void gainNecessary(EndPoint endPoint, ProtocolDataImpl data, byte[] header) throws IOException;

}