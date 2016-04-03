package com.gifisan.nio.component.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.ProtocolData;
import com.gifisan.nio.server.ServerEndPoint;

public class ServerStreamDecoder extends AbstractDecoder{
	
	public ServerStreamDecoder(Charset charset) {
		super(charset);
	}

	public void decode(EndPoint endPoint, ProtocolData data, byte[] header, ByteBuffer buffer) throws IOException {
		decodeStream((ServerEndPoint) endPoint, data, header);
	}
	
	protected void decodeStream(ServerEndPoint endPoint, ProtocolData data, byte[] header) throws IOException {

		if (endPoint.sessionSize() > 1) {
			throw new IOException("unique session can be created when trans strean data");
		}

		int streamLength = getStreamLength(header);

		endPoint.setStreamAvailable(streamLength);
		
	}
}
