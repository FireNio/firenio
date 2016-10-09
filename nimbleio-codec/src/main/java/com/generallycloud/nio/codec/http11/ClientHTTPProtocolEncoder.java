package com.generallycloud.nio.codec.http11;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.http11.future.HttpRequestFuture;
import com.generallycloud.nio.component.SocketChannel;
import com.generallycloud.nio.protocol.IOReadFuture;
import com.generallycloud.nio.protocol.IOWriteFuture;
import com.generallycloud.nio.protocol.IOWriteFutureImpl;
import com.generallycloud.nio.protocol.ProtocolEncoder;

//FIXME jinji
public class ClientHTTPProtocolEncoder implements ProtocolEncoder {

	public IOWriteFuture encode(SocketChannel channel, IOReadFuture readFuture) throws IOException {
		
		HttpRequestFuture future = (HttpRequestFuture) readFuture;

		StringBuilder h = new StringBuilder();

		h.append(future.getMethod());
		h.append(" ");
		h.append(getUrl(future));
		h.append(" HTTP/1.1\r\n");
		h.append("Connection:keep-alive\r\n");
		h.append("Content-Length:0\r\n");
		
		Map<String,String> headers = future.getHeaders();
		
		if (headers != null) {
			Set<Entry<String, String>> hs = headers.entrySet();
			for(Entry<String,String> header : hs){
				h.append(header.getKey());
				h.append(":");
				h.append(header.getValue());
				h.append("\r\n");
			}
		}else{
			h.append("Content-Type:text/html;charset=UTF-8\r\n");
		}
		
		Map<String,String> cookies = future.getCookies();
		
		if (cookies != null) {
			Set<Entry<String, String>> cs = cookies.entrySet();
			for(Entry<String,String> c : cs){
				h.append(c.getKey());
				h.append(":");
				h.append(c.getValue());
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
	
	private String getUrl(HttpRequestFuture future){
		Map<String, String> params = future.getParams();
		if (params == null) {
			return future.getUrl();
		}
		
		String url = future.getUrl();
		
		StringBuilder u = new StringBuilder(url);
		
		int index = url.indexOf("?"); 
		
		if (index < 0) {
			future.setRequestURI(url);
			u.append("?");
		}else{
			future.setRequestURI(url.substring(0,index));
			// /test?aa=b&b=c
		}
		
		Set<Entry<String, String>> ps = params.entrySet();
		for(Entry<String,String> p : ps){
			u.append(p.getKey());
			u.append("=");
			u.append(p.getValue());
			u.append("&");
		}
		
		return u.toString();
	}
}
