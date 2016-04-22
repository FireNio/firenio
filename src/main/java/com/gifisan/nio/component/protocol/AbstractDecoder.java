package com.gifisan.nio.component.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.gifisan.nio.component.EndPoint;

public abstract class AbstractDecoder implements Decoder {

	protected Charset			charset	= null;

	public AbstractDecoder(Charset charset) {
		this.charset = charset;
	}

	protected int gainStreamLength(byte[] header) {
		int v0 = (header[5] & 0xff);
		int v1 = (header[6] & 0xff) << 8;
		int v2 = (header[7] & 0xff) << 16;
		int v3 = (header[8] & 0xff) << 24;
		return v0 | v1 | v2 | v3;
	}
	
	protected byte gainSessionID(byte[] header) throws IOException {

		byte sessionID = header[0];

		if (sessionID > 3 || sessionID < 0) {
			throw new IOException("invalidate session id");
		}

		return sessionID;

	}

	protected int gainTextLength(byte[] header) {
		int v0 = (header[2] & 0xff);
		int v1 = (header[3] & 0xff) << 8;
		int v2 = (header[4] & 0xff) << 16;
		return v0 | v1 | v2;
	}
	
	protected String gainServiceName(EndPoint endPoint, byte[] header) throws IOException {

		int serviceNameLength = header[1];
		
		if (serviceNameLength == 0) {

			throw new IOException("service name is empty");
		}

		ByteBuffer buffer = endPoint.read(serviceNameLength);

		byte[] bytes = buffer.array();

		return new String(bytes, 0, serviceNameLength);
	}
}
