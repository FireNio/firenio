package com.generallycloud.nio.codec.protobuf;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.base.BaseProtocolDecoder;
import com.generallycloud.nio.codec.protobuf.future.ProtobufReadFutureImpl;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;

public class ProtobufProtocolDecoder implements ProtocolDecoder {

	public ChannelReadFuture decode(SocketSession session, ByteBuf buffer) throws IOException {
		
		ByteBuf buf = session.getByteBufAllocator().allocate(BaseProtocolDecoder.PROTOCOL_HEADER);

		buf.read(buffer);

		byte _type = buffer.getByte(0);

		int type = (_type & 0xff) >> 6;

		if (type == BaseProtocolDecoder.PROTOCOL_PING) {
			return new ProtobufReadFutureImpl(session.getContext()).setPING();
		} else if (type == BaseProtocolDecoder.PROTOCOL_PONG) {
			return new ProtobufReadFutureImpl(session.getContext()).setPONG();
		}

		return new ProtobufReadFutureImpl(session, buf);
	}

}
