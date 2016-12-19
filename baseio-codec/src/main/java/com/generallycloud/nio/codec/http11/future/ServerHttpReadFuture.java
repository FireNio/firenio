package com.generallycloud.nio.codec.http11.future;

import java.util.HashMap;
import java.util.Map;

import com.generallycloud.nio.Encoding;
import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSession;

public class ServerHttpReadFuture extends AbstractHttpReadFuture {

	public ServerHttpReadFuture(SocketSession session, ByteBuf readBuffer) {
		super(session, readBuffer);
		this.params = new HashMap<String, String>();
	}

	protected ServerHttpReadFuture(SocketChannelContext context) {
		super(context);
	}

	@Override
	protected void setDefaultResponseHeaders(Map<String, String> headers) {
		
		if (context.getEncoding() == Encoding.GBK) {
			headers.put("Content-Type", "text/plain;charset=gbk");
		}else{
			headers.put("Content-Type", "text/plain;charset=utf-8");
		}
		headers.put("Connection", "keep-alive");
	}

	@Override
	protected void parseContentType(String contentType) {

		if (!StringUtil.isNullOrBlank(contentType)) {

			if (CONTENT_APPLICATION_URLENCODED.equals(contentType)) {

				this.contentType = CONTENT_APPLICATION_URLENCODED;

			} else if (CONTENT_TYPE_TEXT_PLAIN.equals(contentType)) {

				this.contentType = CONTENT_TYPE_TEXT_PLAIN;

			} else if (contentType.startsWith("multipart/form-data;")) {

				int index = KMP_BOUNDARY.match(contentType);

				if (index != -1) {
					boundary = contentType.substring(index + 9);
				}

				this.contentType = CONTENT_TYPE_MULTIPART;
			} else {
				// FIXME other content-type
			}
		} else {
			this.contentType = CONTENT_APPLICATION_URLENCODED;
		}
	}

	@Override
	protected void parseFirstLine(String line) {

		String[] array = line.split(" ");

		if (array.length == 3) {
			this.method = array[0];
			this.setRequestURL(array[1]);
			this.version = array[2];
		} else if (array.length == 4) {
			this.method = array[0];
			this.setRequestURL(array[2]);
			this.version = array[3];
		} else if (array.length == 5) {
			this.method = array[0];
			this.setRequestURL(array[3]);
			this.version = array[4];
		} else {
			throw new IllegalArgumentException("http header first line breaked,msg is:" + line);
		}
	}
}
