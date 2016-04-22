package com.gifisan.nio.component;

import java.io.IOException;

/**
 * <pre>
 * [0       ~              9]
 *  0       = 类型 [2=心跳，0=TEXT，1=MULTI]
 *  1       = session id
 *  2       = service name的长度
 *  3,4,5   = text content的长度
 *  6,7,8,9 = stream content的长度
 * </pre>
 * 
 */
public interface ProtocolDecoder {

	public static final byte	BEAT		= 2;
	public static final byte	MULTI	= 1;
	public static final byte	TEXT		= 0;

	public abstract IOReadFuture decode(EndPoint endPoint) throws IOException;

	public abstract IOReadFuture doDecodeExtend(EndPoint endPoint, byte type) throws IOException;

}