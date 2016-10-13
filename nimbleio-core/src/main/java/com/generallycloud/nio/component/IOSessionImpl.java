package com.generallycloud.nio.component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
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

public class IOSessionImpl implements IOSession {

	private static final Logger		logger		= LoggerFactory.getLogger(IOSessionImpl.class);

	private Object					attachment;
	private NIOContext				context;
	private SocketChannel			socketChannel;
	private Integer				sessionID;
	private DatagramChannel			datagramChannel;
	private Object[]				attachments;
	private EventLoop				eventLoop;
	private long					creationTime	= System.currentTimeMillis();
	private long					lastAccess;
	private HashMap<Object, Object>	attributes	= new HashMap<Object, Object>();

	public IOSessionImpl(SocketChannel channel, Integer sessionID) {
		this.context = channel.getContext();
		this.socketChannel = channel;
		this.sessionID = sessionID;
		this.attachments = new Object[context.getSessionAttachmentSize()];
		// 这里认为在第一次Idle之前，连接都是畅通的
		this.lastAccess = this.creationTime + context.getSessionIdleTime();
		this.eventLoop = context.getEventLoopGroup().getNext();
	}

	public void clearAttributes() {
		attributes.clear();
	}

	public boolean isClosed() {
		return !isOpened();
	}

	public EventLoop getEventLoop() {
		return eventLoop;
	}

	public String getProtocolID() {
		return socketChannel.getProtocolFactory().getProtocolID();
	}

	public void close() {

		synchronized (this) {

			if (isClosed()) {
				return;
			}

			physicalClose(datagramChannel);

			physicalClose(socketChannel);
		}
		
		fireClosed();
	}
	
	private void fireClosed(){

		SessionEventListenerWrapper listenerWrapper = context.getSessionEventListenerStub();

		for (; listenerWrapper != null;) {
			try {
				listenerWrapper.sessionClosed(this);
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			}
			listenerWrapper = listenerWrapper.nextListener();
		}
	}

	private void physicalClose(Channel channel) {

		if (channel == null) {
			return;
		}

		try {
			socketChannel.physicalClose();
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
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
			
			if (handle == null) {
				handle = context.getIOEventHandleAdaptor();
			}

			handle.exceptionCaught(this, future, new DisconnectException("disconnected"), IOEventState.WRITE);
			
			return;
		}

		IOWriteFuture writeFuture = null;

		try {

			ProtocolEncoder encoder = socketChannel.getProtocolEncoder();

			IOReadFuture ioReadFuture = (IOReadFuture) future;
			
			ioReadFuture.update(this);
			
			writeFuture = encoder.encode(socketChannel, ioReadFuture);

			ioReadFuture.flush();

			socketChannel.offer(writeFuture);

		} catch (Exception e) {

			ReleaseUtil.release(writeFuture);

			logger.debug(e.getMessage(), e);

			IOEventHandle handle = future.getIOEventHandle();

			handle.exceptionCaught(this, future, e, IOEventState.WRITE);
		}
	}

	public void flush(IOWriteFuture future) throws IOException {

		try {

			socketChannel.offer(future);

		} catch (Exception e) {

			ReleaseUtil.release(future);

			ReadFuture readFuture = future.getReadFuture();

			logger.debug(e.getMessage(), e);

			IOEventHandle handle = readFuture.getIOEventHandle();

			handle.exceptionCaught(this, readFuture, e, IOEventState.WRITE);
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

	public NIOContext getContext() {
		return context;
	}

	public long getCreationTime() {
		return this.creationTime;
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

	public SocketChannel getSocketChannel() {
		return socketChannel;
	}

	public DatagramChannel getDatagramChannel() {
		return datagramChannel;
	}

	public boolean isBlocking() {
		return socketChannel.isBlocking();
	}

	public Object removeAttribute(Object key) {
		return attributes.remove(key);
	}

	public void setAttachment(Object attachment) {
		this.attachment = attachment;
	}

	public void setAttachment(int index, Object attachment) {

		this.attachments[index] = attachment;
	}

	public void setAttribute(Object key, Object value) {
		attributes.put(key, value);
	}

	public void setSessionID(Integer sessionID) {
		this.sessionID = sessionID;
	}

	public void setDatagramChannel(DatagramChannel datagramChannel) {

		if (this.datagramChannel != null && this.datagramChannel != datagramChannel) {
			throw new IllegalArgumentException("datagram channel setted");
		}

		this.datagramChannel = datagramChannel;
	}

	public void active() {
		this.lastAccess = System.currentTimeMillis();
	}

	public long getLastAccessTime() {
		return lastAccess;
	}
	
	public boolean isOpened() {
		return socketChannel.isOpened();
	}

	public String toString() {
		return socketChannel.toString();
	}

	public ProtocolEncoder getProtocolEncoder() {
		return socketChannel.getProtocolEncoder();
	}

	public void fireOpend() {

		SessionEventListenerWrapper listenerWrapper = context.getSessionEventListenerStub();

		for (; listenerWrapper != null;) {
			try {
				listenerWrapper.sessionOpened(this);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			listenerWrapper = listenerWrapper.nextListener();
		}
	}

}
