package com.generallycloud.nio.component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.HashMap;

import com.generallycloud.nio.DisconnectException;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.IOEventHandle.IOEventState;
import com.generallycloud.nio.component.concurrent.EventLoop;
import com.generallycloud.nio.protocol.IOReadFuture;
import com.generallycloud.nio.protocol.IOWriteFuture;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ReadFuture;

public abstract class SessionImpl implements Session {

	private static final Logger		logger		= LoggerFactory.getLogger(SessionImpl.class);

	protected Object					attachment;
	protected Object[]					attachments;
	protected BaseContext				context;
	protected long					lastAccess;
	protected Integer					sessionID;
	protected EventLoop				eventLoop;
	protected SocketChannel				socketChannel;
	protected DatagramChannel			datagramChannel;
	protected HashMap<Object, Object>		attributes	= new HashMap<Object, Object>();
	protected long					creationTime	= System.currentTimeMillis();

	public SessionImpl(SocketChannel channel, Integer sessionID) {
		this.context = channel.getContext();
		this.socketChannel = channel;
		this.sessionID = sessionID;
		this.attachments = new Object[context.getSessionAttachmentSize()];
		// 这里认为在第一次Idle之前，连接都是畅通的
		this.lastAccess = this.creationTime + context.getSessionIdleTime();
		this.eventLoop = context.getEventLoopGroup().getNext();
	}

	public void active() {
		this.lastAccess = System.currentTimeMillis();
	}

	public void clearAttributes() {
		attributes.clear();
	}

	public void flush(ReadFuture future) throws IOException {

		if (future == null) {
			throw new IllegalStateException("null future");
		}

		if (future.flushed()) {
			throw new IllegalStateException("flushed already");
		}

		SocketChannel socketChannel = this.socketChannel;

		if (!socketChannel.isOpened()) {

			IOEventHandle handle = future.getIOEventHandle();

			handle.exceptionCaught(this, future, new DisconnectException("disconnected"), IOEventState.WRITE);

			return;
		}

		IOWriteFuture writeFuture = null;

		try {

			ProtocolEncoder encoder = socketChannel.getProtocolEncoder();

			IOReadFuture ioReadFuture = (IOReadFuture) future;

			writeFuture = encoder.encode(context, ioReadFuture);

			ioReadFuture.flush();

			flush(writeFuture);

		} catch (Exception e) {

			ReleaseUtil.release(writeFuture);

			logger.debug(e.getMessage(), e);

			IOEventHandle handle = future.getIOEventHandle();

			handle.exceptionCaught(this, future, e, IOEventState.WRITE);
		}
	}

	public Object getAttachment() {
		return attachment;
	}

	public Object getAttachment(int index) {

		return attachments[index];
	}

	public Object getAttribute(Object key) {
		return attributes.get(key);
	}

	public HashMap<Object, Object> getAttributes() {
		return attributes;
	}

	public BaseContext getContext() {
		return context;
	}

	public long getCreationTime() {
		return this.creationTime;
	}

	public DatagramChannel getDatagramChannel() {
		return datagramChannel;
	}

	public Charset getEncoding() {
		return context.getEncoding();
	}

	public EventLoop getEventLoop() {
		return eventLoop;
	}

	public long getLastAccessTime() {
		return lastAccess;
	}

	public String getLocalAddr() {
		return socketChannel.getLocalAddr();
	}

	public String getLocalHost() {
		return socketChannel.getLocalHost();
	}

	public int getLocalPort() {
		return socketChannel.getLocalPort();
	}

	public InetSocketAddress getLocalSocketAddress() {
		return socketChannel.getLocalSocketAddress();
	}

	public int getMaxIdleTime() throws SocketException {
		return socketChannel.getMaxIdleTime();
	}

	public ProtocolEncoder getProtocolEncoder() {
		return socketChannel.getProtocolEncoder();
	}

	public String getProtocolID() {
		return socketChannel.getProtocolFactory().getProtocolID();
	}

	public String getRemoteAddr() {
		return socketChannel.getRemoteAddr();
	}

	public String getRemoteHost() {
		return socketChannel.getRemoteHost();
	}

	public int getRemotePort() {
		return socketChannel.getRemotePort();
	}

	public InetSocketAddress getRemoteSocketAddress() {
		return socketChannel.getRemoteSocketAddress();
	}

	public Integer getSessionID() {
		return sessionID;
	}

	public boolean isBlocking() {
		return socketChannel.isBlocking();
	}

	public boolean isClosed() {
		return !isOpened();
	}

	public boolean isOpened() {
		return socketChannel.isOpened();
	}

	public Object removeAttribute(Object key) {
		return attributes.remove(key);
	}

	public void setAttachment(int index, Object attachment) {

		this.attachments[index] = attachment;
	}

	public void setAttachment(Object attachment) {
		this.attachment = attachment;
	}

	public void setAttribute(Object key, Object value) {
		attributes.put(key, value);
	}

	public void setDatagramChannel(DatagramChannel datagramChannel) {

		if (this.datagramChannel != null && this.datagramChannel != datagramChannel) {
			throw new IllegalArgumentException("datagram channel setted");
		}

		this.datagramChannel = datagramChannel;
	}

	public void setSessionID(Integer sessionID) {
		this.sessionID = sessionID;
	}

	public String toString() {
		return socketChannel.toString();
	}

}
