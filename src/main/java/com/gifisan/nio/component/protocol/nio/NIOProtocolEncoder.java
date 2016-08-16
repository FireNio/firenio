package com.gifisan.nio.component.protocol.nio;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.gifisan.nio.common.MathUtil;
import com.gifisan.nio.component.ByteArrayInputStream;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.protocol.ProtocolEncoder;
import com.gifisan.nio.component.protocol.future.ByteArrayWriteFuture;
import com.gifisan.nio.component.protocol.future.IOWriteFuture;
import com.gifisan.nio.component.protocol.future.MultiWriteFuture;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.component.protocol.future.TextWriteFuture;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;

// >> 右移N位
// << 左移N位
public class NIOProtocolEncoder implements ProtocolEncoder {

	private final int	PROTOCOL_HADER		= NIOProtocolDecoder.PROTOCOL_HADER;
	private final int	STREAM_BEGIN_INDEX	= NIOProtocolDecoder.STREAM_BEGIN_INDEX;

	private void calc_text(byte[] header, int text_length) {
		header[5] = (byte) (text_length & 0xff);
		header[6] = (byte) ((text_length >> 8) & 0xff);
		header[7] = (byte) ((text_length >> 16) & 0xff);
	}

	private void calc_future_id(byte[] header, int future_id) {
		header[1] = (byte) (future_id & 0xff);
		header[2] = (byte) ((future_id >> 8) & 0xff);
		header[3] = (byte) ((future_id >> 16) & 0xff);
	}

	private void calc_stream(byte[] header, int stream_length) {

		MathUtil.int2Byte(header, stream_length, STREAM_BEGIN_INDEX);
	}

	// data with content
	protected ByteBuffer encode_text(int future_id, byte[] service_name_array, byte[] text_array) {

		if (text_array == null || text_array.length == 0) {
			return encode_none(future_id, service_name_array);
		}

		int text_length = text_array.length;
		int service_name_length = service_name_array.length;
		int all_length = text_length + service_name_length + PROTOCOL_HADER;

		ByteBuffer buffer = ByteBuffer.allocate(all_length);

		byte[] header = new byte[PROTOCOL_HADER];

		header[0] = NIOProtocolDecoder.TYPE_TEXT;
		header[4] = (byte) service_name_length;

		calc_future_id(header, future_id);
		calc_text(header, text_length);

		buffer.put(header);
		buffer.put(service_name_array);
		buffer.put(text_array);
		return buffer;
	}

	private ByteBuffer encode_stream(int future_id, byte[] service_name_array, int stream_length) {

		int service_name_length = service_name_array.length;

		ByteBuffer buffer = ByteBuffer.allocate(PROTOCOL_HADER + service_name_length);

		byte[] header = new byte[PROTOCOL_HADER];

		header[0] = NIOProtocolDecoder.TYPE_STREAM;
		header[4] = (byte) service_name_length;

		calc_future_id(header, future_id);
		calc_stream(header, stream_length);

		buffer.put(header);
		buffer.put(service_name_array);
		return buffer;

	}

	// data with stream
	protected ByteBuffer encode_all(int future_id, byte[] service_name_array, byte[] text_array, int stream_length) {

		if (text_array == null || text_array.length == 0) {
			return encode_stream(future_id, service_name_array, stream_length);
		}

		int textLength = text_array.length;
		int service_name_length = service_name_array.length;
		int all_length = textLength + service_name_length + PROTOCOL_HADER;

		ByteBuffer buffer = ByteBuffer.allocate(all_length);

		byte[] header = new byte[PROTOCOL_HADER];

		header[0] = NIOProtocolDecoder.TYPE_MULTI;
		header[4] = (byte) service_name_length;

		calc_future_id(header, future_id);
		calc_text(header, textLength);
		calc_stream(header, stream_length);

		buffer.put(header);
		buffer.put(service_name_array);
		buffer.put(text_array);
		return buffer;

	}

	private ByteBuffer encode_none(int future_id, byte[] service_name_array) {

		int service_name_length = service_name_array.length;

		ByteBuffer buffer = ByteBuffer.allocate(PROTOCOL_HADER + service_name_length);

		byte[] header = new byte[PROTOCOL_HADER];

		header[0] = NIOProtocolDecoder.TYPE_TEXT;
		header[4] = (byte) service_name_length;

		calc_future_id(header, future_id);

		buffer.put(header);
		buffer.put(service_name_array);
		return buffer;
	}
	
	public IOWriteFuture encode(TCPEndPoint endPoint, ReadFuture readFuture) throws IOException {
		
		Session session = endPoint.getSession();
		NIOReadFuture ioReadFuture = (NIOReadFuture) readFuture;
		
		
		Integer future_id = ioReadFuture.getFutureID();
		String service_name = ioReadFuture.getServiceName();
		byte [] text_array = ioReadFuture.getWriteBuffer().toByteArray();
		InputStream inputStream = ioReadFuture.getInputStream();

		byte[] service_name_array = service_name.getBytes(session.getContext().getEncoding());

		if (service_name.length() > 127) {
			throw new IllegalArgumentException("service name too long ," + service_name);
		}

		if (inputStream != null) {

			int data_length = inputStream.available();

			ByteBuffer textBuffer = encode_all(future_id, service_name_array, text_array, data_length);

			textBuffer.flip();

			if (inputStream.getClass() != ByteArrayInputStream.class) {

				return new MultiWriteFuture(endPoint, readFuture, textBuffer, inputStream);
			}

			return new ByteArrayWriteFuture(endPoint, readFuture, textBuffer, (ByteArrayInputStream) inputStream);

		}

		ByteBuffer textBuffer = encode_text(future_id, service_name_array, text_array);

		textBuffer.flip();

		return new TextWriteFuture(endPoint, readFuture, textBuffer);
	}

}
