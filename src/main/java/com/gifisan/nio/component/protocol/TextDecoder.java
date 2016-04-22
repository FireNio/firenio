package com.gifisan.nio.component.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.ReadFuture;
import com.gifisan.nio.server.session.Session;
import com.gifisan.nio.service.TextReadFuture;

public class TextDecoder extends AbstractDecoder implements Decoder{
	
	public TextDecoder(Charset charset) {
		super(charset);
	}

	public ReadFuture decode(EndPoint endPoint, byte[] header) throws IOException {

		byte sessionID = gainSessionID(header);
		
		int textLength = gainTextLength(header);

		Session session = endPoint.getSession(sessionID);
		
		ByteBuffer textBuffer = ByteBuffer.allocate(textLength);
		
		String serviceName = gainServiceName(endPoint, header);
		
		return new TextReadFuture(textBuffer, session, serviceName);
		
	}
}
