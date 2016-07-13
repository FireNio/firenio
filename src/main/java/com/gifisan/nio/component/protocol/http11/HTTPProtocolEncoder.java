package com.gifisan.nio.component.protocol.http11;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.protocol.ProtocolEncoder;
import com.gifisan.nio.component.protocol.future.IOWriteFuture;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.component.protocol.future.TextWriteFuture;
import com.gifisan.nio.component.protocol.http11.future.Cookie;
import com.gifisan.nio.component.protocol.http11.future.DefaultHTTPReadFuture;
import com.gifisan.nio.component.protocol.http11.future.HttpHeader;

public class HTTPProtocolEncoder implements ProtocolEncoder {

	public IOWriteFuture encode(TCPEndPoint endPoint, ReadFuture readFuture) throws IOException {
		
		DefaultHTTPReadFuture future = (DefaultHTTPReadFuture) readFuture;

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
		
		
		List<Cookie> cookieList = future.getCookieList();
		
		if (cookieList != null) {
			for(Cookie c : cookieList){
				h.append("\n");
				h.append("Set-Cookie:");
				h.append(c.toString());
			}
		}
		
		List<HttpHeader> headerList = future.getHeaderList();
		
		if (headerList != null) {
			for(HttpHeader header : headerList){
				h.append("\n");
				h.append(header.getName());
				h.append(":");
				h.append(header.getValue());
			}
		}
		
		h.append("\n\n");
		
		ByteBuffer buffer = ByteBuffer.allocate(h.length() + o.size());

		buffer.put(h.toString().getBytes(endPoint.getContext().getEncoding()));
		buffer.put(o.toByteArray(), 0, o.size());
		
		buffer.flip();

		TextWriteFuture textWriteFuture = new TextWriteFuture(endPoint, readFuture, buffer);

		return textWriteFuture;
	}

}
