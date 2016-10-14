package com.generallycloud.nio.codec.http11.future;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.Session;

public class ServerHttpReadFuture extends AbstractHttpReadFuture {

	public ServerHttpReadFuture(Session session, HttpHeaderParser httpHeaderParser, ByteBuffer readBuffer) {
		super(session, httpHeaderParser, readBuffer);
		this.params = new HashMap<String, String>();
	}

	protected void decodeHeader(byte[] source_array, int length, int pos) throws IOException {

		if (contentLength < 1) {

			body_complete = true;

		} else if (contentLength < 1 << 21) {

			this.setHasOutputStream(true);

			int buffer_size = contentLength > 1024 * 256 ? 1024 * 256 : contentLength;

			this.body_buffer = ByteBuffer.allocate(buffer_size);

			this.outputStream = new BufferedOutputStream(contentLength);

			this.read_length = length - pos;

			this.outputStream.write(source_array, pos, read_length);

		} else {

			this.setHasOutputStream(true);

			this.body_buffer = ByteBuffer.allocate(1024 * 256);

			IOEventHandleAdaptor eventHandle = session.getContext().getIOEventHandleAdaptor();

			try {
				eventHandle.accept(session, this);
			} catch (Exception e) {
				throw new IOException(e.getMessage(), e);
			}

			if (this.outputStream == null) {

				throw new IOException("none outputstream");
			}

			read_length = length - pos;

			outputStream.write(source_array, pos, read_length);
		}
	}

	protected void decodeBody() {

		BufferedOutputStream o = (BufferedOutputStream) outputStream;

		if (HttpHeaderParser.CONTENT_APPLICATION_URLENCODED.equals(contentType)) {
			// FIXME encoding
			String paramString = new String(o.toByteArray(), session.getContext().getEncoding());

			parseParamString(paramString);
		} else {
			// FIXME 解析BODY中的内容
		}

		body_complete = true;
	}

	protected void setDefaultResponseHeaders(Map<String, String> headers) {
		headers.put("Content-Type", "text/plain");
		headers.put("Connection", "keep-alive");
	}

}
