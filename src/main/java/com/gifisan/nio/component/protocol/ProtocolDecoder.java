package com.gifisan.nio.component.protocol;

import java.io.IOException;

import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.future.IOReadFuture;

/**
 * <pre>
 * [0       ~              8]
 *  0       = 类型 [3=心跳，0=TEXT，1=STREAM，2=MULTI]
 *  1       = service name的长度
 *  2,3,4   = text content的长度
 *  5,6,7,8 = stream content的长度
 * </pre>
 * 
 */
public interface ProtocolDecoder {

	public static final byte	TYPE_HTTP		= 71;
	public static final byte	TYPE_BEAT		= 3;
	public static final byte	TYPE_MULTI		= 2;
	public static final byte	TYPE_STREAM		= 1;
	public static final byte	TYPE_TEXT		= 0;
	public static final int	PROTOCOL_HADER	= 9;

	public abstract IOReadFuture decode(TCPEndPoint endPoint) throws IOException;

}