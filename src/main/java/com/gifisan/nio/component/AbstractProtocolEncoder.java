package com.gifisan.nio.component;

import java.nio.ByteBuffer;

public abstract class AbstractProtocolEncoder implements ProtocolEncoder{

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
	protected ByteBuffer encodeText(byte sessionID, byte[] textArray) {
		
		if (textArray == null || textArray.length == 0) {
			
			return encodeNone(sessionID);
		}
		
		int textLength = textArray.length;
		int allLength = textLength + 10;

		ByteBuffer buffer = ByteBuffer.allocate(allLength);

		// >> 右移N位
		// << 左移N位
		byte[] header = new byte[10];
		header[0] = ProtocolDecoder.TEXT;
		header[1] = sessionID;
		calcText(header, textLength);

		buffer.put(header);
		buffer.put(textArray);
		return buffer;
	}


	private ByteBuffer encodeStream(byte sessionID, int streamLength) {

		ByteBuffer buffer = ByteBuffer.allocate(10);

		// >> 右移N位
		// << 左移N位
		byte[] header = new byte[10];
		header[0] = ProtocolDecoder.MULTI;
		header[1] = sessionID;
		calcStream(header, streamLength);

		buffer.put(header);
		return buffer;

	}

	// data with stream
	protected ByteBuffer encodeAll(byte sessionID, byte[] textArray, int streamLength) {

		if (textArray == null || textArray.length == 0) {

			return encodeStream(sessionID, streamLength);
		}

		int textLength = textArray.length;
		int allLength = textLength + 10;

		ByteBuffer buffer = ByteBuffer.allocate(allLength);

		// >> 右移N位
		// << 左移N位
		byte[] header = new byte[10];
		header[0] = ProtocolDecoder.MULTI;
		header[1] = sessionID;
		calcText(header, textLength);
		calcStream(header, streamLength);

		buffer.put(header);
		buffer.put(textArray);
		return buffer;

	}

	private ByteBuffer encodeNone(byte sessionID) {

		int allLength = 10;

		ByteBuffer buffer = ByteBuffer.allocate(allLength);

		// >> 右移N位
		// << 左移N位
		byte[] header = new byte[10];
		header[0] = ProtocolDecoder.TEXT;
		header[1] = sessionID;
		buffer.put(header);
		return buffer;
	}
	
	
}
