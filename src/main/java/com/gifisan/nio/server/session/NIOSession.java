package com.gifisan.nio.server.session;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.nio.charset.Charset;

import com.gifisan.nio.Attachment;
import com.gifisan.nio.Encoding;
import com.gifisan.nio.FlushedException;
import com.gifisan.nio.component.AttributesImpl;
import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.IOExceptionHandle;
import com.gifisan.nio.component.NormalServiceAcceptor;
import com.gifisan.nio.component.ProtocolEncoder;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.server.selector.ServiceAcceptorJob;
import com.gifisan.nio.service.WriteFuture;

public class NIOSession extends AttributesImpl implements IOSession {

	private ServiceAcceptorJob			acceptor			= null;
	private Attachment					attachment		= null;
	private ServerContext				context			= null;
	private long						creationTime		= System.currentTimeMillis();
	private ProtocolEncoder				encoder			= null;
	private EndPoint					endPoint			= null;
	private EndPointWriter				endPointWriter		= null;
	private boolean					flushed			= false;
	private InputStream					inputStream		= null;
	private SessionEventListenerWrapper	lastListener		= null;
	private SessionEventListenerWrapper	listenerStub		= null;
	private boolean					scheduled			= false;
	private BufferedOutputStream			textCache			= new BufferedOutputStream();
	private byte						sessionID			= 0;

	public NIOSession(ServerContext context, EndPoint endPoint, byte sessionID) {
		this.sessionID = sessionID;
		this.endPoint = endPoint;
		this.acceptor = new NormalServiceAcceptor(this, context.getFilterService());
	}

	public void addEventListener(SessionEventListener listener) {
		if (this.listenerStub == null) {
			this.listenerStub = new SessionEventListenerWrapper(listener);
			this.lastListener = this.listenerStub;
		} else {
			this.lastListener.setNext(new SessionEventListenerWrapper(listener));
		}
	}

	public void attach(Attachment attachment) {
		this.attachment = attachment;
	}

	public Attachment attachment() {
		return this.attachment;
	}

	public void destroyImmediately() {

		SessionEventListenerWrapper listenerWrapper = this.listenerStub;

		for (; listenerWrapper != null;) {
			listenerWrapper.onDestroy(this);
			listenerWrapper = listenerWrapper.nextListener();
		}

	}

	public void disconnect() {
		// FIXME ......
		this.endPoint.endConnect();
	}
	
	public void flush() throws IOException {
		flush(null);
	}

	public void flush(IOExceptionHandle handle) throws IOException {
		if (flushed) {
			throw new FlushedException("flushed already");
		}

		if (!endPoint.isOpened()) {
			throw new IOException("channel closed");
		}

		this.flushed = true;

		this.scheduled = true;
		
		WriteFuture future = encoder.encode(this, textCache.toByteArray(), inputStream, handle);
		
		this.inputStream = null;
		
		this.endPointWriter.offer(future);
	}

	public boolean flushed() {
		return flushed;
	}

	public long getCreationTime() {
		return this.creationTime;
	}

	public EndPoint getEndPoint() {
		return endPoint;
	}

	public String getLocalAddr() {
		return endPoint.getLocalAddr();
	}

	public String getLocalHost() {
		return endPoint.getLocalHost();
	}

	public int getLocalPort() {
		return endPoint.getLocalPort();
	}

	public int getMaxIdleTime() throws SocketException {
		return endPoint.getMaxIdleTime();
	}

	public String getRemoteAddr() {
		return endPoint.getRemoteAddr();
	}

	public String getRemoteHost() {
		return endPoint.getRemoteHost();
	}

	public int getRemotePort() {
		return endPoint.getRemotePort();
	}

	public ServerContext getContext() {
		return context;
	}

	public boolean isBlocking() {
		return endPoint.isBlocking();
	}

	public boolean isOpened() {
		return endPoint.isOpened();
	}

	public void schdule() {
		this.scheduled = true;
	}

	public boolean schduled() {
		return scheduled;
	}

	public void setInputStream(InputStream inputStream) throws IOException {
		this.inputStream = inputStream;
	}

	public ServiceAcceptorJob getServiceAcceptorJob() {
		return acceptor;
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

	public byte getSessionID() {
		return sessionID;
	}
	
	

}
