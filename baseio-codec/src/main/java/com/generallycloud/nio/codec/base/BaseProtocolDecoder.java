package com.generallycloud.nio.codec.base;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.base.future.BaseReadFutureImpl;
import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.protocol.IOReadFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;

/**
 * <pre>
 * 
 *  B0：
 *  +-------------------------------------------------+
 *  |                      B0                         |
 *  +   -     -     -     -     -     -     -     -   +
 *  |   0     1     2     3     4     5     6     7   | 
 *  +   -     -     -     -     -     -     -     -   +
 *  |   Message  | PUSH|                              |
 *  |   T Y P E  | TYPE|                              |
 *  +-------------------------------------------------+
 *  
 *  B0:0-1	: 报文类型 [0=UNKONW,1=PACKET,2=BEAT.PING,3=BEAT.PONG]
 *  B0:2  	: 推送类型 [0=PUSH,1=BRODCAST]
 *  B0:3-7	: 预留
 *  B1		: service name  length
 *  B2  - B5 	: future  id
 *  B6  - B9 	: session id
 *  B10 - B13 	: hash    code
 *  B14 - B15 	：text          length
 *  B16 - B19 	：binary        length
 * 
 * </pre>
 */
public class BaseProtocolDecoder implements ProtocolDecoder {
	
	public static final int	PROTOCOL_HADER			= 20;

	public static final int	PROTOCOL_PACKET			= 1;
	public static final int	PROTOCOL_PING				= 2;
	public static final int	PROTOCOL_PONG				= 3;

	public static final int	FUTURE_ID_BEGIN_INDEX		= 2;
	public static final int	SESSION_ID_BEGIN_INDEX		= 6;
	public static final int	HASH_BEGIN_INDEX			= 10;
	public static final int	TEXT_BEGIN_INDEX			= 14;
	public static final int	BINARY_BEGIN_INDEX		= 16;

	public IOReadFuture decode(IOSession session, ByteBuffer buffer) throws IOException {

		ByteBuf buf = session.getContext().getHeapByteBufferPool().allocate(PROTOCOL_HADER);

		buf.read(buffer);

		byte _type = buffer.get(0);

		int type = (_type & 0xff) >> 6;

		if (type == PROTOCOL_PING) {
			return new BaseReadFutureImpl(session.getContext()).setPING();
		} else if (type == PROTOCOL_PONG) {
			return new BaseReadFutureImpl(session.getContext()).setPONG();
		}

		return new BaseReadFutureImpl(session, buf);
	}

}
