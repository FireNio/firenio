package com.generallycloud.nio.codec.fixedlength;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.common.MathUtil;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ChannelWriteFutureImpl;
import com.generallycloud.nio.protocol.ProtocolEncoder;

public class FixedLengthProtocolEncoder implements ProtocolEncoder {

	public ChannelWriteFuture encode(BaseContext context, ChannelReadFuture future) throws IOException {

		if (future.isHeartbeat()) {

			int value = future.isPING() ? FixedLengthProtocolDecoder.PROTOCOL_PING
					: FixedLengthProtocolDecoder.PROTOCOL_PONG;

			byte[] array = new byte[4];

			MathUtil.int2Byte(array, value, 0);

			ByteBuf buffer = context.getByteBufAllocator().allocate(4);

			buffer.put(array);

			buffer.flip();

			return new ChannelWriteFutureImpl(future, buffer);
		}

		BufferedOutputStream outputStream = future.getWriteBuffer();

		int size = outputStream.size();

		ByteBuf buffer = context.getByteBufAllocator().allocate(size + 4);

		byte[] size_array = new byte[4];

		MathUtil.int2Byte(size_array, size, 0);

		buffer.put(size_array);

		buffer.put(outputStream.array(), 0, size);

		buffer.flip();

		return new ChannelWriteFutureImpl(future, buffer);
	}
}
