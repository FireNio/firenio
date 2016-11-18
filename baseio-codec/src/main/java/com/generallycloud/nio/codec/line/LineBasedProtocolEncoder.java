package com.generallycloud.nio.codec.line;

import java.io.IOException;
import java.nio.charset.Charset;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.codec.line.future.LineBasedReadFuture;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ChannelWriteFutureImpl;
import com.generallycloud.nio.protocol.ProtocolEncoder;

public class LineBasedProtocolEncoder implements ProtocolEncoder {

	private byte	lineBase	= '\n';

	public ChannelWriteFuture encode(ByteBufAllocator allocator, ChannelReadFuture future) throws IOException {

		LineBasedReadFuture f = (LineBasedReadFuture) future;

		String write_text = f.getWriteText();

		if (StringUtil.isNullOrBlank(write_text)) {
			throw new IOException("null write text");
		}
		
		Charset charset = future.getContext().getEncoding();

		byte[] text_array = write_text.getBytes(charset);

		int size = text_array.length;

		ByteBuf buf = allocator.allocate(size + 1);

		buf.put(text_array, 0, size);

		buf.putByte(lineBase);

		return new ChannelWriteFutureImpl(future, buf.flip());
	}

}
