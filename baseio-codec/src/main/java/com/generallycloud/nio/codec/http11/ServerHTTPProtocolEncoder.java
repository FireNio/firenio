package com.generallycloud.nio.codec.http11;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.http11.future.Cookie;
import com.generallycloud.nio.codec.http11.future.ServerHttpReadFuture;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.protocol.IOReadFuture;
import com.generallycloud.nio.protocol.IOWriteFuture;
import com.generallycloud.nio.protocol.IOWriteFutureImpl;
import com.generallycloud.nio.protocol.ProtocolEncoder;

public class ServerHTTPProtocolEncoder implements ProtocolEncoder {

	public IOWriteFuture encode(IOSession session, IOReadFuture readFuture) throws IOException {
		
		ServerHttpReadFuture future = (ServerHttpReadFuture) readFuture;

		BufferedOutputStream o = readFuture.getWriteBuffer();

		StringBuilder h = new StringBuilder();

		h.append("HTTP/1.1 ");
		h.append(future.getStatus().getHeaderText());
		h.append("\r\n");
		h.append("Server: baseio/0.0.1\r\n");
		h.append("Content-Length:");
		h.append(o.size());
		h.append("\r\n");
		
		Map<String,String> headers = future.getResponseHeaders();
		
		if (headers != null) {
			Set<Entry<String, String>> hs = headers.entrySet();
			for(Entry<String,String> header : hs){
				h.append(header.getKey());
				h.append(":");
				h.append(header.getValue());
				h.append("\r\n");
			}
		}

		List<Cookie> cookieList = future.getCookieList();
		
		if (cookieList != null) {
			for(Cookie c : cookieList){
				h.append("Set-Cookie:");
				h.append(c.toString());
				h.append("\r\n");
			}
		}
		
		h.append("\r\n");
		
		int size = o.size();
		
		ByteBuf buffer = session.getContext().getHeapByteBufferPool().allocate(h.length() + size);
		
		buffer.put(h.toString().getBytes(session.getContext().getEncoding()));
		
		if (size != 0) {
			buffer.put(o.toByteArray(), 0, o.size());
		}
		
		buffer.flip();

		IOWriteFutureImpl textWriteFuture = new IOWriteFutureImpl(session, readFuture, buffer);

		return textWriteFuture;
	}

}
