package com.generallycloud.nio.component.protocol.http11.future;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.Session;

public class ClientHttpReadFuture extends AbstractHttpReadFuture {

	public ClientHttpReadFuture(Session session, HttpHeaderParser httpHeaderParser, ByteBuffer readBuffer) {
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
				throw new IOException(e.getMessage(),e);
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
	
	public void setRequestURI(String requestURI){
		this.requestURI = requestURI;
	}
	
	public void setHeader(String name, String value) {
		if (response_headers == null) {
			response_headers = new HashMap<String, String>();
			request_headers.put("content-Type", "application/x-www-form-urlencoded");
		}
		response_headers.put(name, value);
	}
}
