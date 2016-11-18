package com.generallycloud.nio.codec.http11;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.buffer.EmptyByteBuf;
import com.generallycloud.nio.codec.http11.future.Cookie;
import com.generallycloud.nio.codec.http11.future.ServerHttpReadFuture;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ChannelWriteFutureImpl;
import com.generallycloud.nio.protocol.ProtocolEncoder;

public class ServerHTTPProtocolEncoder implements ProtocolEncoder {

	public ChannelWriteFuture encode(ByteBufAllocator allocator, ChannelReadFuture readFuture) throws IOException {
		
		ServerHttpReadFuture f = (ServerHttpReadFuture) readFuture;

		String write_text = f.getWriteText();
		
		byte [] text_array;
		
		BufferedOutputStream os = f.getBinaryBuffer();
		
		Charset charset = readFuture.getContext().getEncoding();
		
		int length;
		
		if (StringUtil.isNullOrBlank(write_text)) {
			
			text_array = EmptyByteBuf.EMPTY_BYTEBUF.array();
			
			length = 0;
			
			if (os != null) {
				
				length = os.size();
				
				text_array = os.array();
			}
			
		}else{
			
			text_array = write_text.getBytes(charset);
			
			length = text_array.length;
			
			if (os != null) {
				
				int size = os.size();
				int newLength = length + size;
				
				byte [] newArray = new byte[newLength];
				System.arraycopy(text_array, 0, newArray, 0, length);
				System.arraycopy(os.array(), 0, newArray, length, size);
				
				text_array = newArray;
				length = newLength;
			}
		}
		
		StringBuilder h = new StringBuilder();

		h.append("HTTP/1.1 ");
		h.append(f.getStatus().getHeaderText());
		h.append("\r\n");
		h.append("Server: baseio/0.0.1\r\n");
		h.append("Content-Length:");
		h.append(length);
		h.append("\r\n");
		
		Map<String,String> headers = f.getResponseHeaders();
		
		if (headers != null) {
			Set<Entry<String, String>> hs = headers.entrySet();
			for(Entry<String,String> header : hs){
				h.append(header.getKey());
				h.append(":");
				h.append(header.getValue());
				h.append("\r\n");
			}
		}

		List<Cookie> cookieList = f.getCookieList();
		
		if (cookieList != null) {
			for(Cookie c : cookieList){
				h.append("Set-Cookie:");
				h.append(c.toString());
				h.append("\r\n");
			}
		}
		
		h.append("\r\n");
		
		ByteBuf buf = allocator.allocate(h.length() + length);
		
		buf.put(h.toString().getBytes(charset));
		
		if (length != 0) {
			buf.put(text_array, 0, length);
		}
		
		return new ChannelWriteFutureImpl(readFuture, buf.flip());
	}

}
