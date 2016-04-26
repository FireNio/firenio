package com.gifisan.nio.server.session;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.FlushedException;
import com.gifisan.nio.client.IOWriteFuture;
import com.gifisan.nio.component.AbstractSession;
import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.IOExceptionHandle;
import com.gifisan.nio.component.IOReadFuture;
import com.gifisan.nio.component.ReadFuture;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.server.ServerServiceAcceptor;

public class ServerSession extends AbstractSession implements IOSession {

	public ServerSession(EndPoint endPoint, byte sessionID) {
		super(endPoint, sessionID);
		
		this.context = (ServerContext) endPoint.getContext();

		this.serviceAcceptor = new ServerServiceAcceptor(this, context.getFilterService());
	}

	private ServerContext				context			= null;
	private BufferedOutputStream			textCache			= new BufferedOutputStream();
	private InputStream 				inputStream 		= null;
	private IOExceptionHandle 			handle 			= null;
	private ServerServiceAcceptor 		serviceAcceptor 	= null;
	
	public void flush(ReadFuture future) throws IOException {
		if (((IOReadFuture) future).flushed()) {
			throw new FlushedException("flushed already");
		}

		if (!endPoint.isOpened()) {
			throw new IOException("channel closed");
		}

		IOWriteFuture writeFuture = encoder.encode(this,future.getServiceName(), textCache.toByteArray(), inputStream, handle);
		
		this.textCache.reset();
		
		this.inputStream = null;
		
		this.handle = null;
		
		this.endPointWriter.offer(writeFuture);
	}
	
	public ServerContext getContext() {
		return context;
	}

	public void write(byte b) throws IOException {
		textCache.write(b);
	}

	public void write(byte[] bytes) throws IOException {
		textCache.write(bytes);
	}

	public void write(byte[] bytes, int offset, int length) throws IOException {
		textCache.write(bytes, offset, length);
	}

	public void write(String content) {
		byte[] bytes = content.getBytes(Encoding.DEFAULT);
		textCache.write(bytes);
	}

	public void write(String content, Charset encoding) {
		byte[] bytes = content.getBytes(encoding);
		textCache.write(bytes);
	}

	public void write(InputStream inputStream, IOExceptionHandle handle) throws IOException {
		if (inputStream != null) {
			throw new IOException("multi inputstream");
		}
		
		this.inputStream = inputStream;
		this.handle = handle;
	}
	
	public ServerServiceAcceptor getServiceAcceptor() {
		return serviceAcceptor;
	}
}
