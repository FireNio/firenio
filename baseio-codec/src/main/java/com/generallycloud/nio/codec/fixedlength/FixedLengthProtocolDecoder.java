package com.generallycloud.nio.codec.fixedlength;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.fixedlength.future.FixedLengthReadFutureImpl;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.IOReadFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;

/**
 * <pre>
 *  B0 - B3：
 *  +-----------------+-----------------+-----------------+-----------------+
 *  |        B0                B1                B2               B3        |
 *  + - - - - - - - - + - - - - - - - - + - - - - - - - - + - - - - - - - - +
 *  | 0 1 2 3 4 5 6 7   0 1 2 3 4 5 6 7   0 1 2 3 4 5 6 7   0 1 2 3 4 5 6 7 |
 *  | - - - - - - - - + - - - - - - - - + - - - - - - - - + - - - - - - - - +
 *  |                                                                       |
 *  |                          Data-length(P0-P31)                          |
 *  |                                                                       |
 *  |                                                                       |
 *  +-----------------+-----------------+-----------------+-----------------+
 *  
 *  Data-length:-1表示心跳PING,-2表示心跳PONG,正数为报文长度
 *  注意: 无论是否是心跳报文，报文头长度固定为4个字节
 * 
 * </pre>
 */
public class FixedLengthProtocolDecoder implements ProtocolDecoder {

	public static final int	PROTOCOL_HEADER	= 4;

	public static final int	PROTOCOL_PING		= -1;

	public static final int	PROTOCOL_PONG		= -2;

	public IOReadFuture decode(SocketSession session, ByteBuf buffer) throws IOException {
		
		return new FixedLengthReadFutureImpl(session,session.getContext().getHeapByteBufferPool().allocate(PROTOCOL_HEADER));
	}

}
