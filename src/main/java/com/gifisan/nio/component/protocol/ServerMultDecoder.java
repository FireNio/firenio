package com.gifisan.nio.component.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.ProtocolData;
import com.gifisan.nio.server.ServerEndPoint;

public class ServerMultDecoder extends ServerStreamDecoder {

	public ServerMultDecoder(Charset charset) {
		super(charset);
	}

	public void decode(EndPoint endPoint, ProtocolData data, byte[] header, ByteBuffer buffer) throws IOException {

		decodeText(endPoint, data, buffer);

		decodeStream((ServerEndPoint) endPoint, data, header);
	}
	
}
