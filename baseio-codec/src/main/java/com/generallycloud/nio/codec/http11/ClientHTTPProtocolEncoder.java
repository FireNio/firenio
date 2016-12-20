/*
 * Copyright 2015 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package com.generallycloud.nio.codec.http11;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.codec.http11.future.Cookie;
import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ChannelWriteFutureImpl;
import com.generallycloud.nio.protocol.ProtocolEncoder;

//FIXME post
public class ClientHTTPProtocolEncoder implements ProtocolEncoder {

	@Override
	public ChannelWriteFuture encode(ByteBufAllocator allocator, ChannelReadFuture readFuture) throws IOException {

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

		ByteBuf buf = allocator.allocate(h.length());

		buf.put(h.toString().getBytes(readFuture.getContext().getEncoding()));

		return new ChannelWriteFutureImpl(readFuture, buf.flip());
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
