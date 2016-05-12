package com.gifisan.nio.component.protocol.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.ByteArrayInputStream;
import com.gifisan.nio.component.IOEventHandle;
import com.gifisan.nio.component.IOWriteFuture;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.future.ByteArrayWriteFuture;
import com.gifisan.nio.component.future.MultiWriteFuture;
import com.gifisan.nio.component.future.TextWriteFuture;

public class DefaultTCPProtocolEncoder implements ProtocolEncoder{

	private void calcText(byte[] header, int textLength) {
		header[3] = (byte) (textLength & 0xff);
		header[4] = (byte) ((textLength >> 8) & 0xff);
		header[5] = (byte) ((textLength >> 16) & 0xff);
	}

	private void calcStream(byte[] header, int streamLength) {
		header[6] = (byte) (streamLength & 0xff);
		header[7] = (byte) ((streamLength >> 8) & 0xff);
		header[8] = (byte) ((streamLength >> 16) & 0xff);
		header[9] = (byte) (streamLength >>> 24);
	}

	// data with content
	protected ByteBuffer encodeText(byte sessionID,byte [] serviceNameArray, byte[] textArray) {
		
		if (textArray == null || textArray.length == 0) {
			
			return encodeNone(sessionID,serviceNameArray);
		}
		
		int textLength = textArray.length;
		int serviceNameLength = serviceNameArray.length;
		int allLength = textLength + serviceNameLength + 10;

		ByteBuffer buffer = ByteBuffer.allocate(allLength);

		// >> 右移N位
		// << 左移N位
		byte[] header = new byte[10];
		header[0] = ProtocolDecoder.TEXT;
		header[1] = sessionID;
		header[2] = (byte) serviceNameLength;
		calcText(header, textLength);

		buffer.put(header);
		buffer.put(serviceNameArray);
		buffer.put(textArray);
		return buffer;
	}


	private ByteBuffer encodeStream(byte sessionID,byte [] serviceNameArray, int streamLength) {

		int serviceNameLength = serviceNameArray.length;
		
		ByteBuffer buffer = ByteBuffer.allocate(10 + serviceNameLength);

		// >> 右移N位
		// << 左移N位
		byte[] header = new byte[10];
		header[0] = ProtocolDecoder.STREAM;
		header[1] = sessionID;
		header[2] = (byte) serviceNameLength;
		calcStream(header, streamLength);

		buffer.put(header);
		buffer.put(serviceNameArray);
		return buffer;

	}

	// data with stream
	protected ByteBuffer encodeAll(byte sessionID,byte [] serviceNameArray, byte[] textArray, int streamLength) {

		if (textArray == null || textArray.length == 0) {

			return encodeStream(sessionID,serviceNameArray, streamLength);
		}

		int textLength = textArray.length;
		int serviceNameLength = serviceNameArray.length;
		int allLength = textLength + serviceNameLength + 10;

		ByteBuffer buffer = ByteBuffer.allocate(allLength);

		// >> 右移N位
		// << 左移N位
		byte[] header = new byte[10];
		header[0] = ProtocolDecoder.MULTI;
		header[1] = sessionID;
		header[2] = (byte) serviceNameLength;
		calcText(header, textLength);
		calcStream(header, streamLength);

		buffer.put(header);
		buffer.put(serviceNameArray);
		buffer.put(textArray);
		return buffer;

	}

	private ByteBuffer encodeNone(byte sessionID,byte [] serviceNameArray) {

		int serviceNameLength = serviceNameArray.length;

		ByteBuffer buffer = ByteBuffer.allocate(10 + serviceNameLength);

		// >> 右移N位
		// << 左移N位
		byte[] header = new byte[10];
		header[0] = ProtocolDecoder.TEXT;
		header[1] = sessionID;
		header[2] = (byte) serviceNameLength;
		buffer.put(header);
		buffer.put(serviceNameArray);
		return buffer;
	}
	
	public IOWriteFuture encode(TCPEndPoint endPoint, Session session, String serviceName, byte[] array,
			InputStream inputStream, IOEventHandle handle) throws IOException {

		byte[] serviceNameArray = serviceName.getBytes(session.getContext().getEncoding());

		if (inputStream != null) {

			int dataLength = inputStream.available();

			ByteBuffer textBuffer = encodeAll(session.getLogicSessionID(), serviceNameArray, array, dataLength);

			textBuffer.flip();

			if (inputStream.getClass() != ByteArrayInputStream.class) {

				return new MultiWriteFuture(endPoint, session, serviceName, textBuffer, array, inputStream, handle);
			}

			return new ByteArrayWriteFuture(endPoint, session, serviceName, textBuffer, array,
					(ByteArrayInputStream) inputStream, handle);

		}

		ByteBuffer textBuffer = encodeText(session.getLogicSessionID(), serviceNameArray, array);

		textBuffer.flip();

		return new TextWriteFuture(endPoint, session, serviceName, textBuffer, array, handle);
	}

	
}
