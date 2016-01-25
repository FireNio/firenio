package com.gifisan.mtp.client;

import java.nio.ByteBuffer;

public class ProtocolEncoder {

	private void encodeNecessary(byte[] header, byte sessionID, int snLength) {
		header[1] = sessionID;
		header[2] = (byte) snLength;
	}

	private void encodeContent(byte[] header, int contentLength) {
		header[3] = (byte) (contentLength & 0xff);
		header[4] = (byte) ((contentLength >> 8) & 0xff);
		header[5] = (byte) ((contentLength >> 16) & 0xff);
	}

	private void encodeStream(byte[] header, int streamLength) {
		header[6] = (byte) (streamLength & 0xff);
		header[7] = (byte) ((streamLength >> 8) & 0xff);
		header[8] = (byte) ((streamLength >> 16) & 0xff);
		header[9] = (byte) (streamLength >>> 24);
	}

	// text without content
	public ByteBuffer encode(byte sessionID, byte[] serviceName) {
		int snLength = serviceName.length;
		int bLength = snLength + 10;

		ByteBuffer buffer = ByteBuffer.allocate(bLength);

		byte[] header = new byte[10];
		header[0] = 0;
		encodeNecessary(header, sessionID, snLength);

		buffer.put(header);
		buffer.put(serviceName);

		return buffer;
	}

	// text with content
	public ByteBuffer encode(byte sessionID, byte[] serviceName, byte[] content) {
		int snLength = serviceName.length;
		int contentLength = content.length;
		int bLength = snLength + contentLength + 10;

		ByteBuffer buffer = ByteBuffer.allocate(bLength);

		byte[] header = new byte[10];
		header[0] = 0;
		encodeNecessary(header, sessionID, snLength);
		encodeContent(header, contentLength);

		buffer.put(header);
		buffer.put(serviceName);
		buffer.put(content);

		return buffer;
	}

	// data without content
	public ByteBuffer encode(byte sessionID, byte[] serviceName, int streamLength) {
		int snLength = serviceName.length;
		int bLength = snLength + 10;

		ByteBuffer buffer = ByteBuffer.allocate(bLength);

		byte[] header = new byte[10];
		header[0] = 1;
		encodeNecessary(header, sessionID, snLength);
		encodeStream(header, streamLength);

		buffer.put(header);
		buffer.put(serviceName);
		return buffer;
	}

	// data with content
	public ByteBuffer encode(byte sessionID, byte[] serviceName, byte[] content, int streamLength) {
		int snLength = serviceName.length;
		int contentLength = content.length;
		int bLength = snLength + contentLength + 10;

		ByteBuffer buffer = ByteBuffer.allocate(bLength);

		// >> 右移N位
		// << 左移N位
		byte[] header = new byte[10];
		header[0] = 2;
		encodeNecessary(header, sessionID, snLength);
		encodeContent(header, contentLength);
		encodeStream(header, streamLength);

		buffer.put(header);
		buffer.put(serviceName);
		buffer.put(content);
		return buffer;
	}

}
