package com.generallycloud.nio.codec.fixedlength;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.fixedlength.future.FixedLengthReadFuture;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ChannelWriteFutureImpl;
import com.generallycloud.nio.protocol.ProtocolEncoder;

public class FixedLengthProtocolEncoder implements ProtocolEncoder {

	public ChannelWriteFuture encode(BaseContext context, ChannelReadFuture future) throws IOException {

		if (future.isHeartbeat()) {

			int value = future.isPING() ? FixedLengthProtocolDecoder.PROTOCOL_PING
					: FixedLengthProtocolDecoder.PROTOCOL_PONG;

			ByteBuf buffer = context.getByteBufAllocator().allocate(4);

			buffer.putInt(value);

			buffer.flip();

			return new ChannelWriteFutureImpl(future, buffer);
		}
		
		FixedLengthReadFuture f = (FixedLengthReadFuture) future;

		String write_text = f.getWriteText();
		
		if (StringUtil.isNullOrBlank(write_text)) {
			throw new IOException("null write text");
		}
		
		byte [] text_array = write_text.getBytes(context.getEncoding());

		int size = text_array.length;

		ByteBuf buf = context.getByteBufAllocator().allocate(size + 4);

		buf.putInt(size);

		buf.put(text_array, 0, size);

		buf.flip();

		return new ChannelWriteFutureImpl(future, buf);
	}
}
