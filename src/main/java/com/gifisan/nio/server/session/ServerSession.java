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
import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.IOExceptionHandle;
import com.gifisan.nio.component.ProtocolEncoder;
import com.gifisan.nio.component.ServerServiceAcceptor;
import com.gifisan.nio.server.ServerContext;

public class ServerSession extends AbstractSession implements IOSession {

	public ServerSession(EndPoint endPoint, byte sessionID) {
		super(endPoint, sessionID);
		
		this.context = (ServerContext) endPoint.getContext();

		this.endPointWriter = context.getEndPointWriter();
		
		this.serviceAcceptor = new ServerServiceAcceptor(this, context.getFilterService());
	}

	private ServerContext				context			= null;
	private ProtocolEncoder				encoder			= null;
	private EndPointWriter				endPointWriter		= null;
	private boolean					flushed			= false;
	private boolean					scheduled			= false;
	private BufferedOutputStream			textCache			= new BufferedOutputStream();

	

	public void flush() throws IOException {
		flush(null);
	}
	
	public void flush(IOExceptionHandle handle) throws IOException {
		flush(null, handle);
	}

	public void flush(InputStream inputStream ,IOExceptionHandle handle) throws IOException {
		if (flushed) {
			throw new FlushedException("flushed already");
		}

		if (!endPoint.isOpened()) {
			throw new IOException("channel closed");
		}

		this.flushed = true;

		this.scheduled = true;
		
		IOWriteFuture future = encoder.encode(this, textCache.toByteArray(), inputStream, handle);
		
		this.endPointWriter.offer(future);
	}

	public boolean flushed() {
		return flushed;
	}

	public ServerContext getContext() {
		return context;
	}

	public void schdule() {
		this.scheduled = true;
	}

	public boolean schduled() {
		return scheduled;
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

}
