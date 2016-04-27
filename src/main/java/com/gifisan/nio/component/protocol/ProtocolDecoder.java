package com.gifisan.nio.component.protocol;

import java.io.IOException;

import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.future.IOReadFuture;

/**
 * <pre>
 * [0       ~              9]
 *  0       = 类型 [3=心跳，0=TEXT，1=STREAM，2=MULTI]
 *  1       = session id
 *  2       = service name的长度
 *  3,4,5   = text content的长度
 *  6,7,8,9 = stream content的长度
 * </pre>
 * 
 */
public interface ProtocolDecoder {

	public static final byte	HTTP		= 71;
	public static final byte	BEAT		= 3;
	public static final byte	MULTI	= 2;
	public static final byte	STREAM	= 1;
	public static final byte	TEXT		= 0;

	public abstract IOReadFuture decode(EndPoint endPoint) throws IOException;

}