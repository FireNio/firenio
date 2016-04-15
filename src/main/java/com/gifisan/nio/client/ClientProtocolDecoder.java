package com.gifisan.nio.client;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.AbstractProtocolDecoder;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.ProtocolDataImpl;
import com.gifisan.nio.component.ProtocolDecoder;
import com.gifisan.nio.component.protocol.Decoder;

public class ClientProtocolDecoder extends AbstractProtocolDecoder implements ProtocolDecoder {
	
	public ClientProtocolDecoder(Decoder textDecoder,Decoder streamDecoder,Decoder multiDecoder) {
		super(textDecoder, streamDecoder, multiDecoder);
	}

	public void gainNecessary(EndPoint endPoint, ProtocolDataImpl data, byte[] header) throws IOException {
		
	}

	protected boolean decodeTextBuffer(Decoder decoder, EndPoint endPoint, ProtocolDataImpl data, byte[] header)
			throws IOException {
		
		int textLength = getTextLength(header);
		
		if (textLength == 0) {
			
			decoder.decode(endPoint, data, header,null);
			
			return true;
		}
		
		ByteBuffer buffer = endPoint.read(textLength);
		
		decoder.decode(endPoint, data, header,buffer);
		
		return true;	
	}
}
