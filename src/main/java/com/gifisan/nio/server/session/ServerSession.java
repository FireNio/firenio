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
import com.gifisan.nio.component.ReadFuture;
import com.gifisan.nio.component.ServerServiceAcceptor;
import com.gifisan.nio.server.ServerContext;

public class ServerSession extends AbstractSession implements IOSession {

	public ServerSession(EndPoint endPoint, byte sessionID) {
		super(endPoint, sessionID);
		
		this.context = (ServerContext) endPoint.getContext();

		this.endPointWriter = context.getEndPointWriter();
		
		this.encoder = context.getProtocolEncoder();
		
		this.serviceAcceptor = new ServerServiceAcceptor(this, context.getFilterService());
	}

	private ServerContext				context			= null;
	private ProtocolEncoder				encoder			= null;
	private EndPointWriter				endPointWriter		= null;
	private boolean					flushed			= false;
	private boolean					scheduled			= false;//guancha
	private BufferedOutputStream			textCache			= new BufferedOutputStream();
	private InputStream 				inputStream 		= null;
	private IOExceptionHandle 			handle 			= null;
	private ReadFuture					future			= null;
	
	public void flush() throws IOException {
		if (flushed) {
			throw new FlushedException("flushed already");
		}

		if (!endPoint.isOpened()) {
			throw new IOException("channel closed");
		}

		this.flushed = true;

		this.scheduled = true;
		
		IOWriteFuture writeFuture = encoder.encode(this,future.getServiceName(), textCache.toByteArray(), inputStream, handle);
		
		this.inputStream = null;
		
		this.handle = null;
		
		this.endPointWriter.offer(writeFuture);
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

	public void write(InputStream inputStream, IOExceptionHandle handle) throws IOException {
		if (inputStream != null) {
			throw new IOException("multi inputstream");
		}
		
		this.inputStream = inputStream;
		this.handle = handle;
	}
	
	public void update(ReadFuture future){
		this.future = future;
		this.flushed = false;
		this.scheduled = false;
	}

}
