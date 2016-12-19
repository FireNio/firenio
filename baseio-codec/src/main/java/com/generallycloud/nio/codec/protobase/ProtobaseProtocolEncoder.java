package com.generallycloud.nio.codec.protobase;

import java.io.IOException;
import java.nio.charset.Charset;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.buffer.EmptyByteBuf;
import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ChannelWriteFutureImpl;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolException;

public class ProtobaseProtocolEncoder implements ProtocolEncoder {

	private static final byte [] EMPTY_ARRAY = EmptyByteBuf.EMPTY_BYTEBUF.array();

	@Override
	public ChannelWriteFuture encode(ByteBufAllocator allocator, ChannelReadFuture readFuture) throws IOException {

		if (readFuture.isHeartbeat()) {

			byte[] array = new byte[1];

			array[0] = (byte) (readFuture.isPING() ? 
					ProtobaseProtocolDecoder.PROTOCOL_PING
					: ProtobaseProtocolDecoder.PROTOCOL_PONG << 6);

			ByteBuf buf = allocator.allocate(1);

			buf.put(array);

			return new ChannelWriteFutureImpl(readFuture, buf.flip());
		}

		ProtobaseReadFuture f = (ProtobaseReadFuture) readFuture;

		String future_name = f.getFutureName();
		String writeText = f.getWriteText();
		BufferedOutputStream binaryOPS = f.getWriteBinaryBuffer();

		if (StringUtil.isNullOrBlank(future_name)) {
			throw new ProtocolException("future name is empty");
		}
		
		Charset charset = readFuture.getContext().getEncoding();

		byte[] future_name_array = future_name.getBytes(charset);
		byte[] text_array;
		if (StringUtil.isNullOrBlank(writeText)) {
			text_array = EMPTY_ARRAY;
		}else{
			text_array = writeText.getBytes(charset);
		}

		int service_name_length = future_name_array.length;
		int text_length = text_array.length;
		int binary_length = 0;

		if (service_name_length > 255) {
			throw new IllegalArgumentException("service name too long ," + future_name);
		}

		if (binaryOPS != null) {
			binary_length = binaryOPS.size();
		}

		int all_length = ProtobaseProtocolDecoder.PROTOCOL_HEADER 
					+ service_name_length 
					+ text_length 
					+ binary_length;

		ByteBuf buf = allocator.allocate(all_length);

		//01000000 0x40 01100000 0x60
		if (f.isBroadcast()) {
			buf.putByte((byte)60);
		}else{
			buf.putByte((byte)40);
		}

		buf.putByte((byte) (service_name_length));
		buf.putInt(f.getFutureID());
		buf.putInt(f.getSessionID());
		buf.putInt(f.getHashCode());
		buf.putUnsignedShort(text_length);
		buf.putInt(binary_length);

		buf.put(future_name_array);

		if (text_length > 0) {
			buf.put(text_array, 0, text_length);
		}

		if (binary_length > 0) {
			buf.put(binaryOPS.array(), 0, binary_length);
		}

		return new ChannelWriteFutureImpl(readFuture, buf.flip());
	}

}
