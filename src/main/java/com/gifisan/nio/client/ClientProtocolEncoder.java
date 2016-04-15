package com.gifisan.nio.client;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.ProtocolDecoder;

public class ClientProtocolEncoder {

	private void encodeNecessary(byte[] header, byte sessionID, int serviceNameLength) {
		header[1] = sessionID;
		header[2] = (byte) serviceNameLength;
	}

	private void encodeContent(byte[] header, int textLength) {
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
	public ByteBuffer encode(byte sessionID, String serviceName, String text,Charset charset) {

		if (StringUtil.isNullOrBlank(text)) {

			return encode(sessionID, serviceName);
		}
		
		
		byte[] textArray = text.getBytes(charset);
		byte[] serviceNameArray = serviceName.getBytes();

		int serviceNameLength = serviceNameArray.length;
		int textLength = textArray.length;
		int allLength = serviceNameLength + textLength + 10;

		ByteBuffer buffer = ByteBuffer.allocate(allLength);

		// >> 右移N位
		// << 左移N位
		byte[] header = new byte[10];
		header[0] = ProtocolDecoder.TEXT;
		encodeNecessary(header, sessionID, serviceNameLength);
		encodeContent(header, textLength);
//		encodeStream(header, 0);

		buffer.put(header);
		buffer.put(serviceNameArray);
		buffer.put(textArray);
		return buffer;
	}

	// data with stream
	public ByteBuffer encode(byte sessionID, String serviceName, String text, int streamLength,Charset charset) {

		if (StringUtil.isNullOrBlank(text)) {

			return encode(sessionID, serviceName, streamLength);
		}

		byte[] textArray = text.getBytes(charset);
		byte[] serviceNameArray = serviceName.getBytes();

		int serviceNameLength = serviceNameArray.length;
		int textLength = textArray.length;
		int allLength = serviceNameLength + textLength + 10;

		ByteBuffer buffer = ByteBuffer.allocate(allLength);

		// >> 右移N位
		// << 左移N位
		byte[] header = new byte[10];
		header[0] = ProtocolDecoder.MULTI;
		encodeNecessary(header, sessionID, serviceNameLength);
		encodeContent(header, textLength);
		encodeStream(header, streamLength);

		buffer.put(header);
		buffer.put(serviceNameArray);
		buffer.put(textArray);
		return buffer;
	}

	// data with stream
	private ByteBuffer encode(byte sessionID, String serviceName, int streamLength) {

		byte[] serviceNameArray = serviceName.getBytes();

		int serviceNameLength = serviceNameArray.length;
		int allLength = serviceNameLength + 10;

		ByteBuffer buffer = ByteBuffer.allocate(allLength);

		// >> 右移N位
		// << 左移N位
		byte[] header = new byte[10];
		header[0] = ProtocolDecoder.STREAM;
		encodeNecessary(header, sessionID, serviceNameLength);
		// encodeContent(header, 0);
		encodeStream(header, streamLength);

		buffer.put(header);
		buffer.put(serviceNameArray);
		return buffer;
	}

	// data with text
	private ByteBuffer encode(byte sessionID, String serviceName) {

		byte[] serviceNameArray = serviceName.getBytes();

		int serviceNameLength = serviceNameArray.length;
		int allLength = serviceNameLength + 10;

		ByteBuffer buffer = ByteBuffer.allocate(allLength);

		// >> 右移N位
		// << 左移N位
		byte[] header = new byte[10];
		header[0] = ProtocolDecoder.TEXT;
		encodeNecessary(header, sessionID, serviceNameLength);
		// encodeContent(header, 0);
		//encodeStream(header, 0);

		buffer.put(header);
		buffer.put(serviceNameArray);
		return buffer;
	}

}
