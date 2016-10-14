package com.generallycloud.nio.codec.http11;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.http11.future.Cookie;
import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.component.SocketChannel;
import com.generallycloud.nio.protocol.IOReadFuture;
import com.generallycloud.nio.protocol.IOWriteFuture;
import com.generallycloud.nio.protocol.IOWriteFutureImpl;
import com.generallycloud.nio.protocol.ProtocolEncoder;

//FIXME jinji
public class ClientHTTPProtocolEncoder implements ProtocolEncoder {

	public IOWriteFuture encode(SocketChannel channel, IOReadFuture readFuture) throws IOException {

		HttpReadFuture future = (HttpReadFuture) readFuture;

		StringBuilder h = new StringBuilder();

		h.append(future.getMethod());
		h.append(" ");
		h.append(getRequestURI(future));
		h.append(" HTTP/1.1\r\n");

		Map<String, String> headers = future.getResponseHeaders();

		if (headers != null) {
			Set<Entry<String, String>> hs = headers.entrySet();
			for (Entry<String, String> header : hs) {
				h.append(header.getKey());
				h.append(":");
				h.append(header.getValue());
				h.append("\r\n");
			}
		}

		List<Cookie> cookieList = future.getCookieList();

		if (cookieList != null) {
			for (Cookie c : cookieList) {
				h.append("Set-Cookie:");
				h.append(c.toString());
				h.append("\r\n");
			}
		}

		h.append("\r\n");

		ByteBuf buffer = channel.getContext().getHeapByteBufferPool().allocate(h.length());

		buffer.put(h.toString().getBytes(channel.getContext().getEncoding()));

		buffer.flip();

		IOWriteFutureImpl textWriteFuture = new IOWriteFutureImpl(channel, readFuture, buffer);

		return textWriteFuture;
	}

	private String getRequestURI(HttpReadFuture future) {
		Map<String, String> params = future.getRequestParams();
		if (params == null) {
			return future.getRequestURL();
		}

		String url = future.getRequestURI();

		StringBuilder u = new StringBuilder(url);

		u.append("?");

		Set<Entry<String, String>> ps = params.entrySet();
		for (Entry<String, String> p : ps) {
			u.append(p.getKey());
			u.append("=");
			u.append(p.getValue());
			u.append("&");
		}

		return u.toString();
	}
}
