package com.generallycloud.nio.codec.nio;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.common.MathUtil;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.SocketChannel;
import com.generallycloud.nio.protocol.IOReadFuture;
import com.generallycloud.nio.protocol.IOWriteFuture;
import com.generallycloud.nio.protocol.IOWriteFutureImpl;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolException;

public class NIOProtocolEncoder implements ProtocolEncoder {

	private final int	PROTOCOL_HADER		= NIOProtocolDecoder.PROTOCOL_HADER;

	private void calc_text(byte[] header, int text_length) {
		MathUtil.intTo2Byte(header, text_length, NIOProtocolDecoder.TEXT_BEGIN_INDEX);
	}

	private void calc_future_id(byte[] header, int future_id) {
		MathUtil.int2Byte(header, future_id, NIOProtocolDecoder.FUTURE_ID_BEGIN_INDEX);
	}

	private void calc_binary(byte[] header, int binary_length) {
		MathUtil.int2Byte(header, binary_length, NIOProtocolDecoder.BINARY_BEGIN_INDEX);
	}
	
	private void calc_hash(byte[] header, int hash) {
		MathUtil.int2Byte(header, hash, NIOProtocolDecoder.HASH_BEGIN_INDEX);
	}
	
	public IOWriteFuture encode(SocketChannel channel,IOReadFuture readFuture) throws IOException {
		
		if (readFuture.isHeartbeat()) {
			
			byte [] array = new byte[1];

			array [0] = (byte)(readFuture.isPING() 
					? NIOProtocolDecoder.PROTOCOL_PING : 
						NIOProtocolDecoder.PROTOCOL_PONG 
						<< 6);
			
			ByteBuf buffer = channel.getContext().getHeapByteBufferPool().allocate(1);
			
			buffer.put(array);
			
			buffer.flip();
			
			return new IOWriteFutureImpl(channel, readFuture, buffer);
		}
		
		Session session = channel.getSession();
		
		NIOReadFuture f = (NIOReadFuture) readFuture;
		
		Integer future_id = f.getFutureID();
		String future_name = f.getFutureName();
		BufferedOutputStream textOPS = f.getWriteBuffer();
		BufferedOutputStream binaryOPS = f.getWriteBinaryBuffer();
		
		if (StringUtil.isNullOrBlank(future_name)) {
			throw new ProtocolException("future name is empty");
		}
		
		byte[] future_name_array = future_name.getBytes(session.getContext().getEncoding());

		int service_name_length = future_name_array.length;
		int text_length = textOPS.size();
		int binary_length = 0;
		
		if (service_name_length > ((1 << 6) -1)) {
			throw new IllegalArgumentException("service name too long ," + future_name);
		}
		
		if (binaryOPS != null) {
			binary_length = binaryOPS.size();
		}
		
		int all_length = PROTOCOL_HADER + service_name_length + text_length + binary_length;
		
		byte[] header = new byte[PROTOCOL_HADER];

		header[0] = (byte)(service_name_length);

		calc_future_id(header, future_id);
		calc_hash(header, f.getHashCode());
		calc_text(header, text_length);
		calc_binary(header, binary_length);

		ByteBuf buffer = channel.getContext().getHeapByteBufferPool().allocate(all_length);
		
		buffer.put(header);
		buffer.put(future_name_array);
		
		if (text_length > 0) {
			buffer.put(textOPS.array(),0,text_length);
		}
		
		if (binary_length > 0) {
			buffer.put(binaryOPS.array(),0,binary_length);
		}

		buffer.flip();

		return new IOWriteFutureImpl(channel, readFuture, buffer);
	}

}
