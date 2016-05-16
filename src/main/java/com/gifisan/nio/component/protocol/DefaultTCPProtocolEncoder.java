package com.gifisan.nio.component.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.gifisan.nio.common.MathUtil;
import com.gifisan.nio.component.ByteArrayInputStream;
import com.gifisan.nio.component.IOEventHandle;
import com.gifisan.nio.component.IOWriteFuture;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.future.ByteArrayWriteFuture;
import com.gifisan.nio.component.future.MultiWriteFuture;
import com.gifisan.nio.component.future.TextWriteFuture;

public class DefaultTCPProtocolEncoder implements ProtocolEncoder{
	
	private final int PROTOCOL_HADER = ProtocolDecoder.PROTOCOL_HADER;

	private void calcText(byte[] header, int textLength) {
		header[2] = (byte) (textLength & 0xff);
		header[3] = (byte) ((textLength >> 8) & 0xff);
		header[4] = (byte) ((textLength >> 16) & 0xff);
	}

	private void calcStream(byte[] header, int streamLength) {
		
		MathUtil.int2Byte(header, streamLength, 5);
	}

	// data with content
	protected ByteBuffer encodeText(byte [] serviceNameArray, byte[] textArray) {
		
		if (textArray == null || textArray.length == 0) {
			
			return encodeNone(serviceNameArray);
		}
		
		int textLength = textArray.length;
		int serviceNameLength = serviceNameArray.length;
		int allLength = textLength + serviceNameLength + PROTOCOL_HADER;

		ByteBuffer buffer = ByteBuffer.allocate(allLength);

		// >> 右移N位
		// << 左移N位
		byte[] header = new byte[PROTOCOL_HADER];
		header[0] = ProtocolDecoder.TYPE_TEXT;
		header[1] = (byte) serviceNameLength;
		calcText(header, textLength);

		buffer.put(header);
		buffer.put(serviceNameArray);
		buffer.put(textArray);
		return buffer;
	}


	private ByteBuffer encodeStream(byte [] serviceNameArray, int streamLength) {

		int serviceNameLength = serviceNameArray.length;
		
		ByteBuffer buffer = ByteBuffer.allocate(PROTOCOL_HADER + serviceNameLength);

		// >> 右移N位
		// << 左移N位
		byte[] header = new byte[PROTOCOL_HADER];
		header[0] = ProtocolDecoder.TYPE_STREAM;
		header[1] = (byte) serviceNameLength;
		calcStream(header, streamLength);

		buffer.put(header);
		buffer.put(serviceNameArray);
		return buffer;

	}

	// data with stream
	protected ByteBuffer encodeAll(byte [] serviceNameArray, byte[] textArray, int streamLength) {

		if (textArray == null || textArray.length == 0) {

			return encodeStream(serviceNameArray, streamLength);
		}

		int textLength = textArray.length;
		int serviceNameLength = serviceNameArray.length;
		int allLength = textLength + serviceNameLength + PROTOCOL_HADER;

		ByteBuffer buffer = ByteBuffer.allocate(allLength);

		// >> 右移N位
		// << 左移N位
		byte[] header = new byte[PROTOCOL_HADER];
		header[0] = ProtocolDecoder.TYPE_MULTI;
		header[1] = (byte) serviceNameLength;
		calcText(header, textLength);
		calcStream(header, streamLength);

		buffer.put(header);
		buffer.put(serviceNameArray);
		buffer.put(textArray);
		return buffer;

	}

	private ByteBuffer encodeNone(byte [] serviceNameArray) {

		int serviceNameLength = serviceNameArray.length;

		ByteBuffer buffer = ByteBuffer.allocate(PROTOCOL_HADER + serviceNameLength);

		// >> 右移N位
		// << 左移N位
		byte[] header = new byte[PROTOCOL_HADER];
		header[0] = ProtocolDecoder.TYPE_TEXT;
		header[1] = (byte) serviceNameLength;
		buffer.put(header);
		buffer.put(serviceNameArray);
		return buffer;
	}
	
	public IOWriteFuture encode(TCPEndPoint endPoint, Session session, String serviceName, byte[] array,
			InputStream inputStream, IOEventHandle handle) throws IOException {

		byte[] serviceNameArray = serviceName.getBytes(session.getContext().getEncoding());
		
		if (serviceName.length() > 127) {
			throw new IllegalArgumentException("service name too long ,"+serviceName);
		}

		if (inputStream != null) {

			int dataLength = inputStream.available();

			ByteBuffer textBuffer = encodeAll(serviceNameArray, array, dataLength);

			textBuffer.flip();

			if (inputStream.getClass() != ByteArrayInputStream.class) {

				return new MultiWriteFuture(endPoint, session, serviceName, textBuffer, array, inputStream, handle);
			}

			return new ByteArrayWriteFuture(endPoint, session, serviceName, textBuffer, array,
					(ByteArrayInputStream) inputStream, handle);

		}

		ByteBuffer textBuffer = encodeText(serviceNameArray, array);

		textBuffer.flip();

		return new TextWriteFuture(endPoint, session, serviceName, textBuffer, array, handle);
	}

	
}
