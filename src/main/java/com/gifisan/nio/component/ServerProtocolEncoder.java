package com.gifisan.nio.component;

import java.nio.ByteBuffer;

public class ServerProtocolEncoder implements ProtocolEncoder{

	private void encodeText(byte[] header, int textLength) {
		header[3] = (byte) (textLength & 0xff);
		header[4] = (byte) ((textLength >> 8) & 0xff);
		header[5] = (byte) ((textLength >> 16) & 0xff);
	}

	private void encodeStream(byte[] header, int streamLength) {
		header[6] = (byte) (streamLength & 0xff);
		header[7] = (byte) ((streamLength >> 8) & 0xff);
		header[8] = (byte) ((streamLength >> 16) & 0xff);
		header[9] = (byte) (streamLength >>> 24);
	}

	// data with content
	public ByteBuffer encode(byte sessionID, byte[] textArray) {

		int textLength = textArray.length;
		int allLength = textLength + 10;

		ByteBuffer buffer = ByteBuffer.allocate(allLength);

		// >> 右移N位
		// << 左移N位
		byte[] header = new byte[10];
		header[0] = ProtocolDecoder.TEXT;
		header[1] = sessionID;
		encodeText(header, textLength);

		buffer.put(header);
		buffer.put(textArray);
		return buffer;
	}

	private ByteBuffer encodeAll(byte sessionID, byte[] textArray, int streamLength) {

		int textLength = textArray.length;
		int allLength = textLength + 10;

		ByteBuffer buffer = ByteBuffer.allocate(allLength);

		// >> 右移N位
		// << 左移N位
		byte[] header = new byte[10];
		header[0] = ProtocolDecoder.MULTI;
		header[1] = sessionID;
		encodeText(header, textLength);
		encodeStream(header, streamLength);

		buffer.put(header);
		buffer.put(textArray);
		return buffer;

	}

	// data with stream
	public ByteBuffer encode(byte sessionID, byte[] textArray, int streamLength) {

		if (textArray == null || textArray.length == 0) {

			if (streamLength < 1) {
				return encode(sessionID);

			} else {
				return encode(sessionID, streamLength);
			}
		} else {
			if (streamLength < 1) {
				return encode(sessionID, textArray);

			} else {
				return encodeAll(sessionID, textArray, streamLength);
			}
		}
	}

	// data with stream
	private ByteBuffer encode(byte sessionID, int streamLength) {

		int allLength = 10;

		ByteBuffer buffer = ByteBuffer.allocate(allLength);

		// >> 右移N位
		// << 左移N位
		byte[] header = new byte[10];
		header[0] = ProtocolDecoder.STREAM;
		header[1] = sessionID;
		encodeStream(header, streamLength);

		buffer.put(header);
		return buffer;
	}

	// data with text
	private ByteBuffer encode(byte sessionID) {

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
