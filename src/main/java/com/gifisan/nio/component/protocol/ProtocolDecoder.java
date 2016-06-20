package com.gifisan.nio.component.protocol;

import java.io.IOException;

import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.future.IOReadFuture;

/**
 * <pre>
 * [0       ~              11]
 *  0         = 类型 [3=心跳，0=TEXT，1=STREAM，2=MULTI]
 *  1,2,3     = request id的长度
 *  4,        = service name的长度
 *  5,6,7     = text content的长度
 *  8,9,10,11 = stream content的长度
 * </pre>
 * 
 */
public interface ProtocolDecoder {

	public static final byte	TYPE_HTTP					= 71;
	public static final byte	TYPE_BEAT					= 3;
	public static final byte	TYPE_MULTI					= 2;
	public static final byte	TYPE_STREAM					= 1;
	public static final byte	TYPE_TEXT					= 0;
	public static final int	PROTOCOL_HADER				= 12;
	public static final int	SERVICE_NAME_LENGTH_INDEX		= 4;
	public static final int	STREAM_BEGIN_INDEX			= 8;
	public static final int	TEXT_BEGIN_INDEX				= 5;

	public abstract IOReadFuture decode(TCPEndPoint endPoint) throws IOException;

}