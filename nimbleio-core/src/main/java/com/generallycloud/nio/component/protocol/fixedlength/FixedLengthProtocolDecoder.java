package com.generallycloud.nio.component.protocol.fixedlength;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.IOReadFuture;
import com.generallycloud.nio.component.protocol.ProtocolDecoderAdapter;
import com.generallycloud.nio.component.protocol.fixedlength.future.FixedLengthReadFutureImpl;

/**
 * <pre>
 *  B0 - B3：
 *  +-----------------+-----------------+-----------------+-----------------+
 *  |        B0                B1                B2               B3        |
 *  + - - - - - - - - + - - - - - - - - + - - - - - - - - + - - - - - - - - +
 *  | 0 1 2 3 4 5 6 7   0 1 2 3 4 5 6 7   0 1 2 3 4 5 6 7   0 1 2 3 4 5 6 7 |
 *  | - - - - - - - - + - - - - - - - - + - - - - - - - - + - - - - - - - - +
 *  | B|                                                                    |
 *  | E|                       Data-length(P1-P31)                          |
 *  | A|                                                                    |
 *  | T|                                                                    |
 *  +-----------------+-----------------+-----------------+-----------------+
 *  
 *  Beat:1表示心跳，0表示正常报文
 *  Data-length:表示一个Java的Integer，用来表示报文的长度
 *  注意: 无论是否是心跳报文，报文头长度固定为4个字节
 *  
 * </pre>
 */
public class FixedLengthProtocolDecoder extends ProtocolDecoderAdapter {
	
	public static final int	PROTOCOL_HADER				= 4;

	protected ByteBuf allocate(NIOContext context) {
		return context.getHeapByteBufferPool().allocate(PROTOCOL_HADER);
	}

	protected IOReadFuture fetchFuture(Session session, ByteBuf buffer) {
		return new FixedLengthReadFutureImpl(session,buffer);
	}
	
}
