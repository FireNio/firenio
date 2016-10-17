package com.generallycloud.nio.codec.http11.future;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.component.NIOContext;

public class ClientHttpReadFuture extends AbstractHttpReadFuture {

	public ClientHttpReadFuture(NIOContext context,String url,String method){
		super(context);
		this.method = method;
		this.setRequestURL(url);
	}
	
	public ClientHttpReadFuture(IOSession session, HttpHeaderParser httpHeaderParser, ByteBuffer readBuffer) {
		super(session, httpHeaderParser, readBuffer);
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
		body_complete = true;
	}

	protected void setDefaultResponseHeaders(Map<String, String> headers) {
		headers.put("Connection", "keep-alive");
//		headers.put("Content-Length", "0");
	}

	public void updateWebSocketProtocol() {
		session.setProtocolFactory(PROTOCOL_FACTORY);
		session.setProtocolDecoder(WEBSOCKET_PROTOCOL_DECODER);
		session.setProtocolEncoder(WEBSOCKET_PROTOCOL_ENCODER);
	}
}
