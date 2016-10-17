package com.generallycloud.nio.codec.http11.future;

import java.nio.ByteBuffer;
import java.util.Map;

import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.component.NIOContext;

public class ClientHttpReadFuture extends AbstractHttpReadFuture {

	public ClientHttpReadFuture(NIOContext context, String url, String method) {
		super(context);
		this.method = method;
		this.setRequestURL(url);
	}

	public ClientHttpReadFuture(IOSession session, ByteBuffer readBuffer) {
		super(session, readBuffer);
	}

	protected void setDefaultResponseHeaders(Map<String, String> headers) {
		headers.put("Connection", "keep-alive");
	}

	public void updateWebSocketProtocol() {
		session.setProtocolFactory(PROTOCOL_FACTORY);
		session.setProtocolDecoder(WEBSOCKET_PROTOCOL_DECODER);
		session.setProtocolEncoder(WEBSOCKET_PROTOCOL_ENCODER);
	}

	protected void parseContentType(String contentType) {

		if (!StringUtil.isNullOrBlank(contentType)) {

			if (CONTENT_APPLICATION_URLENCODED.equals(contentType)) {

				this.contentType = CONTENT_APPLICATION_URLENCODED;

			} else if (contentType.startsWith("multipart/form-data;")) {

				int index = KMP_BOUNDARY.match(contentType);

				if (index != -1) {
					this.boundary = contentType.substring(index + 9);
				}

				this.contentType = CONTENT_TYPE_MULTIPART;
			} else {
				// FIXME other content-type
			}
		} else {
			this.contentType = CONTENT_APPLICATION_URLENCODED;
		}
	}

	protected void parseFirstLine(String line) {
		String[] array = line.split(" ");
		this.version = array[0];
		this.status = HttpStatus.getHttpStatus(Integer.parseInt(array[1]));
	}
}
