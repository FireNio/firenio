/*
 * Copyright 2015-2017 GenerallyCloud.com
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
import java.nio.charset.Charset;
import java.util.List;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.codec.http11.future.Cookie;
import com.generallycloud.nio.codec.http11.future.ServerHttpReadFuture;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ChannelWriteFutureImpl;

public class ServerHTTPProtocolEncoder extends AbstractHttpProtocolEncoder {

	private static final byte[]	PROTOCOL		= "HTTP/1.1 ".getBytes();
	private static final byte[]	SERVER_CL	= "\r\nServer:baseio/0.0.1\r\nContent-Length:".getBytes();
	private static final byte[]	SET_COOKIE	= "Set-Cookie:".getBytes();


	@Override
	public ChannelWriteFuture encode(ByteBufAllocator allocator, ChannelReadFuture readFuture) throws IOException {

		ServerHttpReadFuture f = (ServerHttpReadFuture) readFuture;

		BufferedOutputStream os = f.getBinaryBuffer();

		if (os != null) {
			return encode(allocator, f, os.size(), os.array());
		}

		String write_text = f.getWriteText();

		Charset charset = readFuture.getContext().getEncoding();

		if (StringUtil.isNullOrBlank(write_text)) {

			return encode(allocator, f, 0, null);
		}

		byte[] text_array = write_text.getBytes(charset);

		return encode(allocator, f, text_array.length, text_array);
	}

	public ChannelWriteFuture encode(ByteBufAllocator allocator, ServerHttpReadFuture f, int length, byte[] array)
			throws IOException {

		ByteBuf buf = allocator.allocate(256);

		buf.put(PROTOCOL);
		buf.put(f.getStatus().getHeaderBinary());
		buf.put(SERVER_CL);
		buf.put(String.valueOf(length).getBytes());
		buf.put(RN);

		writeHeaders(f, buf);
		
		List<Cookie> cookieList = f.getCookieList();

		if (cookieList != null) {
			for (Cookie c : cookieList) {
				writeBuf(buf, SET_COOKIE);
				writeBuf(buf, c.toString().getBytes());
				writeBuf(buf, RN);
			}
		}

		writeBuf(buf, RN);

		if (length != 0) {
			writeBuf(buf, array, 0, length);
		}

		return new ChannelWriteFutureImpl(f, buf.flip());
	}

}
