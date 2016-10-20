package com.generallycloud.nio.codec.protobuf;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.base.BaseProtocolDecoder;
import com.generallycloud.nio.codec.protobuf.future.ProtobufReadFutureImpl;
import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.protocol.IOReadFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;

public class ProtobufProtocolDecoder implements ProtocolDecoder {

	public IOReadFuture decode(IOSession session, ByteBuffer buffer) throws IOException {
		
		ByteBuf buf = session.getContext().getHeapByteBufferPool().allocate(BaseProtocolDecoder.PROTOCOL_HADER);

		buf.read(buffer);

		byte _type = buffer.get(0);

		int type = (_type & 0xff) >> 6;

		if (type == BaseProtocolDecoder.PROTOCOL_PING) {
			return new ProtobufReadFutureImpl(session.getContext()).setPING();
		} else if (type == BaseProtocolDecoder.PROTOCOL_PONG) {
			return new ProtobufReadFutureImpl(session.getContext()).setPONG();
		}

		return new ProtobufReadFutureImpl(session, buf);
	}

}
