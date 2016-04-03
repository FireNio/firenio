package com.gifisan.nio.component.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.gifisan.nio.client.ClientEndPoint;
import com.gifisan.nio.client.ClientResponse;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.ProtocolData;

public class ClientMultDecoder extends ClientStreamDecoder {

	public ClientMultDecoder(Charset charset) {
		super(charset);
	}

	public void decode(EndPoint endPoint, ProtocolData data, byte[] header, ByteBuffer buffer) throws IOException {

		decodeText(endPoint, data, buffer);

		decodeStream((ClientEndPoint) endPoint, (ClientResponse) data, header);
	}
	
}
