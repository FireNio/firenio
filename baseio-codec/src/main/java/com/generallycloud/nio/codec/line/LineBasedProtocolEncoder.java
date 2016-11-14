package com.generallycloud.nio.codec.line;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.line.future.LineBasedReadFuture;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ChannelWriteFutureImpl;
import com.generallycloud.nio.protocol.ProtocolEncoder;

public class LineBasedProtocolEncoder implements ProtocolEncoder {

	private byte	lineBase	= '\n';

	public ChannelWriteFuture encode(BaseContext context, ChannelReadFuture future) throws IOException {

		LineBasedReadFuture f = (LineBasedReadFuture) future;

		String write_text = f.getReadText();

		if (StringUtil.isNullOrBlank(write_text)) {
			throw new IOException("null write text");
		}

		byte[] text_array = write_text.getBytes(context.getEncoding());

		int size = text_array.length;

		ByteBuf buf = context.getByteBufAllocator().allocate(size + 1);

		buf.put(text_array, 0, size);

		buf.putByte(lineBase);

		buf.flip();

		return new ChannelWriteFutureImpl(future, buf);
	}

}
