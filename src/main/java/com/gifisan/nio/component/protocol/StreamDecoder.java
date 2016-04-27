package com.gifisan.nio.component.protocol;

import java.io.IOException;
import java.nio.charset.Charset;

import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.IOReadFuture;
import com.gifisan.nio.component.future.StreamReadFuture;

public class StreamDecoder extends AbstractDecoder {

	public StreamDecoder(Charset charset) {
		super(charset);
	}

	public IOReadFuture decode(EndPoint endPoint, byte[] header) throws IOException {

		byte sessionID = gainSessionID(header);

		int dataLength = gainStreamLength(header);

		Session session = endPoint.getSession(sessionID);

		String serviceName = gainServiceName(endPoint, header);

		return new StreamReadFuture(endPoint, session, serviceName, dataLength);
	}
}
