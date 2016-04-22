package com.gifisan.nio.server.session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.gifisan.nio.Attachment;
import com.gifisan.nio.Encoding;
import com.gifisan.nio.FlushedException;
import com.gifisan.nio.component.AttributesImpl;
import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.ByteArrayInputStream;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.IOExceptionHandle;
import com.gifisan.nio.component.Message;
import com.gifisan.nio.component.NormalServiceAcceptor;
import com.gifisan.nio.component.ProtocolEncoder;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.server.selector.ServiceAcceptorJob;
import com.gifisan.nio.service.ByteArrayWriteFuture;
import com.gifisan.nio.service.MultiWriteFuture;
import com.gifisan.nio.service.TextWriteFuture;

public class NIOSession extends AttributesImpl implements Session {

	private ServiceAcceptorJob			acceptor			= null;
	private Attachment					attachment		= null;
	private ByteArrayInputStream			bInputStream		= null;
	private ServerContext				context			= null;
	private long						creationTime		= System.currentTimeMillis();
	private int						dataLength		= 0;
	private ProtocolEncoder				encoder			= null;
	private EndPoint					endPoint			= null;
	private EndPointWriter				endPointWriter		= null;
	private boolean					flushed			= false;
	private InputStream					inputStream		= null;
	private SessionEventListenerWrapper	lastListener		= null;
	private SessionEventListenerWrapper	listenerStub		= null;
	private boolean					scheduled			= false;
	private OutputStream				serverOutputStream	= null;
	private boolean					stream			= false;
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

	public void flush(Message message, IOExceptionHandle catchWriteException) throws IOException {
		if (flushed) {
			throw new FlushedException("flushed already");
		}

		if (!endPoint.isOpened()) {
			throw new IOException("channel closed");
		}

		this.flushed = true;

		this.scheduled = true;

		this.removeServerOutputStream();

		ByteBuffer textByteBuffer = encoder.encode(sessionID, textCache.toByteArray(), dataLength);

		textByteBuffer.flip();

		if (dataLength > 0) {

			if (bInputStream == null) {

				MultiWriteFuture writer = new MultiWriteFuture(catchWriteException, textByteBuffer, this,
						inputStream);
				this.endPointWriter.offer(writer);
				return;
			}

			ByteArrayWriteFuture writer = new ByteArrayWriteFuture(catchWriteException, textByteBuffer, this,
					bInputStream);
			this.endPointWriter.offer(writer);
			return;
		}

		TextWriteFuture writer = new TextWriteFuture(catchWriteException, textByteBuffer, this);

		this.endPointWriter.offer(writer);

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

	public OutputStream getServerOutputStream() {
		return serverOutputStream;
	}

	public boolean isBlocking() {
		return endPoint.isBlocking();
	}

	public boolean isOpened() {
		return endPoint.isOpened();
	}

	public boolean isStream() {
		return stream;
	}

	public void removeServerOutputStream() {
		this.serverOutputStream = null;
		this.endPoint.resetServerOutputStream();
	}

	public void schdule() {
		this.scheduled = true;
	}

	public boolean schduled() {
		return scheduled;
	}

	public void setEndPoint(EndPoint endPoint) {
		this.endPoint = endPoint;
	}

	public void setInputStream(InputStream inputStream) throws IOException {
		this.dataLength = inputStream.available();
		if (inputStream.getClass() != ByteArrayInputStream.class) {
			this.inputStream = inputStream;
			return;
		}
		this.bInputStream = (ByteArrayInputStream) inputStream;
	}

	public void setServerOutputStream(OutputStream serverOutputStream) {
		this.serverOutputStream = serverOutputStream;
	}

	public void setStream(boolean stream) {
		this.stream = stream;
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
