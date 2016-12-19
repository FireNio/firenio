package com.generallycloud.nio.codec.protobase;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFutureImpl;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ChannelReadFuture;
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
 *  B16 - B19 	：binary        length //FIXME 是否应该设置为两字节？
 *  
 * </pre>
 */
public class ProtobaseProtocolDecoder implements ProtocolDecoder {
	
	public static final int	PROTOCOL_HEADER			= 20;

	public static final int	PROTOCOL_PACKET			= 1;
	public static final int	PROTOCOL_PING				= 2;
	public static final int	PROTOCOL_PONG				= 3;

	private int limit;
	
	public ProtobaseProtocolDecoder(int limit) {
		this.limit = limit;
	}

	@Override
	public ChannelReadFuture decode(SocketSession session, ByteBuf buffer) throws IOException {

		ByteBuf buf = session.getByteBufAllocator().allocate(PROTOCOL_HEADER);

		buf.read(buffer);

		byte _type = buffer.getByte(0);

		int type = (_type & 0xff) >> 6;

		if (type == PROTOCOL_PING) {
			return new ProtobaseReadFutureImpl(session.getContext()).setPING();
		} else if (type == PROTOCOL_PONG) {
			return new ProtobaseReadFutureImpl(session.getContext()).setPONG();
		}

		return new ProtobaseReadFutureImpl(session, buf,limit);
	}

}
