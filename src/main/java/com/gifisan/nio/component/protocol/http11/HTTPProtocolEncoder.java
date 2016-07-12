package com.gifisan.nio.component.protocol.http11;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.protocol.ProtocolEncoder;
import com.gifisan.nio.component.protocol.future.IOWriteFuture;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.component.protocol.future.TextWriteFuture;
import com.gifisan.nio.component.protocol.http11.future.HTTPReadFuture;

public class HTTPProtocolEncoder implements ProtocolEncoder {

	public IOWriteFuture encode(TCPEndPoint endPoint, ReadFuture readFuture) throws IOException {
		
		HTTPReadFuture future = (HTTPReadFuture) readFuture;

		BufferedOutputStream o = readFuture.getWriteBuffer();

		future.getStatus();
		StringBuilder h = new StringBuilder();

		h.append("HTTP/1.1 ");
		h.append(future.getStatus());
		h.append(" OK\n");
		h.append("Server: nimbleio/0.0.1\n");
		h.append("Content-Type:text/html;charset=UTF-8\n");
		h.append("Connection:close\n");
		h.append("Content-Length:");
		h.append(o.size());
		h.append("\n\n");
		
		ByteBuffer buffer = ByteBuffer.allocate(h.length() + o.size());

		buffer.put(h.toString().getBytes(endPoint.getContext().getEncoding()));
		buffer.put(o.toByteArray(), 0, o.size());
		
		buffer.flip();

		TextWriteFuture textWriteFuture = new TextWriteFuture(endPoint, readFuture, buffer);

		return textWriteFuture;
	}

}
