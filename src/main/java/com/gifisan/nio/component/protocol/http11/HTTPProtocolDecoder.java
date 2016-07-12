package com.gifisan.nio.component.protocol.http11;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.protocol.ProtocolDecoder;
import com.gifisan.nio.component.protocol.future.IOReadFuture;
import com.gifisan.nio.component.protocol.http11.future.DefaultHTTPReadFuture;

public class HTTPProtocolDecoder implements ProtocolDecoder {
	
	private ByteBuffer readBuffer = ByteBuffer.allocate(1024 * 4);

	public IOReadFuture decode(TCPEndPoint endPoint) throws IOException {

		ByteBuffer buffer = this.readBuffer;
		
		int length = endPoint.read(buffer);
		
		if (length < 1) {
			if (length == -1) {
				endPoint.endConnect();
			}
			return null;
		}
		
		DefaultHTTPReadFuture future = new DefaultHTTPReadFuture(endPoint.getSession(), buffer);
		
		future.decode(endPoint, buffer);
		
		return future;
	}

}
