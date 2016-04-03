package com.gifisan.nio.component.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.gifisan.nio.client.ClientEndPoint;
import com.gifisan.nio.client.ClientResponse;
import com.gifisan.nio.client.EndPointInputStream;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.ProtocolData;

public class ClientStreamDecoder extends AbstractDecoder{
	
	public ClientStreamDecoder(Charset charset) {
		super(charset);
	}

	public void decode(EndPoint endPoint, ProtocolData data, byte[] header, ByteBuffer buffer) throws IOException {
		decodeStream((ClientEndPoint) endPoint, (ClientResponse) data, header);
	}
	
	protected EndPointInputStream readInputStream(int length, ClientEndPoint endPoint) throws IOException {

		return length == 0 ? null : new EndPointInputStream(endPoint, length);
	}
	
	protected void decodeStream(ClientEndPoint endPoint, ClientResponse data, byte[] header) throws IOException {

		if (endPoint.sessionSize() > 1) {
			throw new IOException("unique session can be created when trans strean data");
		}

		int streamLength = getStreamLength(header);

		EndPointInputStream inputStream = readInputStream(streamLength, endPoint);

		data.setInputStream(inputStream);
		
		endPoint.setInputStream(inputStream);
		
	}
}
