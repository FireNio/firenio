package com.gifisan.nio.component.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.EndPointInputStream;
import com.gifisan.nio.component.InputStream;
import com.gifisan.nio.component.ProtocolData;

public abstract class AbstractDecoder implements Decoder {

	protected Charset			charset	= null;

	public AbstractDecoder(Charset charset) {
		this.charset = charset;
	}

	private int getStreamLength(byte[] header) {
		int v0 = (header[5] & 0xff);
		int v1 = (header[6] & 0xff) << 8;
		int v2 = (header[7] & 0xff) << 16;
		int v3 = (header[8] & 0xff) << 24;
		return v0 | v1 | v2 | v3;
	}
	
	public boolean read(EndPoint endPoint ,ByteBuffer buffer) throws IOException{
		
		endPoint.read(buffer);
		
		return buffer.capacity() == buffer.position();
	}
	
	protected void decodeText(EndPoint endPoint, ProtocolData data,ByteBuffer buffer) throws IOException {
		
		if (buffer == null) {
			return;
		}
		
		byte[] bytes = buffer.array();

		String content = new String(bytes, charset);

		data.setText(content);
	}
	
	private InputStream readInputStream(int length, EndPoint endPoint) throws IOException {

		return length == 0 ? null : new EndPointInputStream(endPoint, length);
	}
	
	protected void decodeStream(EndPoint endPoint, ProtocolData data, byte[] header) throws IOException {

		if (endPoint.sessionSize() > 1) {
			throw new IOException("unique session can be created when trans strean data");
		}

		int streamLength = getStreamLength(header);

		InputStream inputStream = readInputStream(streamLength, endPoint);

		data.setInputStream(inputStream);
		
		endPoint.setInputStream(inputStream);
	}

	public boolean progressRead(EndPoint endPoint ,ByteBuffer buffer) throws IOException{
		
		endPoint.read(buffer);
		
		return buffer.capacity() == buffer.position();
	}
}
