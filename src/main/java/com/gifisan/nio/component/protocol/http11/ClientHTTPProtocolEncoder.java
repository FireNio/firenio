package com.gifisan.nio.component.protocol.http11;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.protocol.ProtocolEncoder;
import com.gifisan.nio.component.protocol.future.IOWriteFuture;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.component.protocol.future.TextWriteFuture;
import com.gifisan.nio.component.protocol.http11.future.DefaultHTTPReadFuture;
import com.gifisan.nio.component.protocol.http11.future.HttpHeader;

public class ClientHTTPProtocolEncoder implements ProtocolEncoder {

	public IOWriteFuture encode(TCPEndPoint endPoint, ReadFuture readFuture) throws IOException {
		
		DefaultHTTPReadFuture future = (DefaultHTTPReadFuture) readFuture;

		StringBuilder h = new StringBuilder();

		h.append(future.getMethod());
		h.append(" ");
		h.append(future.getRequestURI());
		h.append(" HTTP/1.1\r\n");
		h.append("Content-Type:text/html;charset=UTF-8\r\n");
		h.append("Connection:keep-alive\r\n");
		h.append("Content-Length:0\r\n");
		
		List<HttpHeader> headerList = future.getHeaderList();
		
		if (headerList != null) {
			for(HttpHeader header : headerList){
				h.append(header.getName());
				h.append(":");
				h.append(header.getValue());
				h.append("\r\n");
			}
		}
		
		h.append("\r\n");
		
		ByteBuffer buffer = ByteBuffer.allocate(h.length());

		buffer.put(h.toString().getBytes(endPoint.getContext().getEncoding()));
		
		buffer.flip();

		TextWriteFuture textWriteFuture = new TextWriteFuture(endPoint, readFuture, buffer);

		return textWriteFuture;
	}

}
