package com.generallycloud.nio.component.protocol.nio;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.common.MathUtil;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.TCPEndPoint;
import com.generallycloud.nio.component.protocol.IOReadFuture;
import com.generallycloud.nio.component.protocol.IOWriteFuture;
import com.generallycloud.nio.component.protocol.IOWriteFutureImpl;
import com.generallycloud.nio.component.protocol.ProtocolEncoder;
import com.generallycloud.nio.component.protocol.nio.future.NIOReadFuture;

public class NIOProtocolEncoder implements ProtocolEncoder {

	private final int	PROTOCOL_HADER		= NIOProtocolDecoder.PROTOCOL_HADER;
	private final int	BINARY_BEGIN_INDEX	= NIOProtocolDecoder.BINARY_BEGIN_INDEX;

	private void calc_text(byte[] header, int text_length) {
		header[4] = (byte) (text_length & 0xff);
		header[5] = (byte) ((text_length >> 8) & 0xff);
		header[6] = (byte) ((text_length >> 16) & 0xff);
	}

	private void calc_future_id(byte[] header, int future_id) {
		header[1] = (byte) (future_id & 0xff);
		header[2] = (byte) ((future_id >> 8) & 0xff);
		header[3] = (byte) ((future_id >> 16) & 0xff);
	}

	private void calc_stream(byte[] header, int stream_length) {

		MathUtil.int2Byte(header, stream_length, BINARY_BEGIN_INDEX);
	}

	public IOWriteFuture encode(TCPEndPoint endPoint, IOReadFuture readFuture) throws IOException {
		
		if (readFuture.isBeatPacket()) {
			
			byte [] array = new byte[1];

			array [0] = (byte)(NIOProtocolDecoder.TYPE_BEAT << 6);
			
			ByteBuf buffer = endPoint.getContext().getDirectByteBufferPool().allocate(1);
			
			buffer.put(array);
			
			buffer.flip();
			
			return new IOWriteFutureImpl(endPoint, readFuture, buffer);
		}
		
		Session session = endPoint.getSession();
		
		NIOReadFuture nioReadFuture = (NIOReadFuture) readFuture;
		
		Integer future_id = nioReadFuture.getFutureID();
		String service_name = nioReadFuture.getServiceName();
		BufferedOutputStream textOPS = nioReadFuture.getWriteBuffer();
		BufferedOutputStream binaryOPS = nioReadFuture.getWriteBinaryBuffer();
		
		byte[] service_name_array = service_name.getBytes(session.getContext().getEncoding());

		int service_name_length = service_name_array.length;
		int text_length = textOPS.size();
		int binary_length = 0;
		
		if (service_name_length > ((1 << 6) -1)) {
			throw new IllegalArgumentException("service name too long ," + service_name);
		}
		
		if (binaryOPS != null) {
			binary_length = binaryOPS.size();
		}
		
		int all_length = PROTOCOL_HADER + service_name_length + text_length + binary_length;
		
		ByteBuf buffer = endPoint.getContext().getDirectByteBufferPool().allocate(all_length);
		
		byte[] header = new byte[PROTOCOL_HADER];

		header[0] = (byte)(service_name_length);

		calc_future_id(header, future_id);
		
		calc_text(header, text_length);
		calc_stream(header, binary_length);

		buffer.put(header);
		buffer.put(service_name_array);
		
		if (text_length > 0) {
			buffer.put(textOPS.array(),0,text_length);
		}
		
		if (binary_length > 0) {
			buffer.put(binaryOPS.array(),0,binary_length);
		}

		buffer.flip();

		return new IOWriteFutureImpl(endPoint, readFuture, buffer);
	}

}
