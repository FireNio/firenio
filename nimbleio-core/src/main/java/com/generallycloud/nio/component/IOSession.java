package com.generallycloud.nio.component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;

import com.generallycloud.nio.DisconnectException;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.IOEventHandle.IOEventState;
import com.generallycloud.nio.component.concurrent.EventLoop;
import com.generallycloud.nio.component.protocol.IOReadFuture;
import com.generallycloud.nio.component.protocol.IOWriteFuture;
import com.generallycloud.nio.component.protocol.ProtocolEncoder;
import com.generallycloud.nio.component.protocol.ReadFuture;

public class IOSession implements Session {

	private static final Logger			logger		= LoggerFactory.getLogger(IOSession.class);
	
	private Object						attachment;
	private boolean					closed;
	private NIOContext					context;
	private TCPEndPoint					endPoint;
	private Integer					sessionID;
	private UDPEndPoint					udpEndPoint;
	private Object[]					attachments	;
	private EventLoop					eventLoop;
	private long						creationTime	= System.currentTimeMillis();
	private long						lastAccess;
	private HashMap<Object, Object>		attributes	= new HashMap<Object, Object>();

	public IOSession(TCPEndPoint endPoint, Integer sessionID) {
		this.context = endPoint.getContext();
		this.endPoint = endPoint;
		this.sessionID = sessionID;
		this.attachments = new Object[context.getSessionAttachmentSize()];
		//这里认为在第一次Idle之前，连接都是畅通的
		this.lastAccess = this.creationTime + context.getSessionIdleTime();
		this.eventLoop = context.getEventLoopGroup().getNext();
	}

	public void clearAttributes() {
		attributes.clear();
	}

	public boolean closed() {
		return closed;
	}
	
	public EventLoop getEventLoop() {
		return eventLoop;
	}
	
	public String getProtocolID() {
		return endPoint.getProtocolFactory().getProtocolID();
	}

	public void close() {
		
		synchronized (this) {
			
			if (closed) {
				return;
			}

			this.closed = true;
			
			//FIXME
			physicalClose(udpEndPoint);
			
			physicalClose(endPoint);
			
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
	}
	
	private void physicalClose(EndPoint endPoint) {
		
		if (endPoint == null) {
			return;
		}
		
		try {
			endPoint.physicalClose();
		} catch (Throwable e) {
			logger.error(e.getMessage(),e);
		}
	}

	public void flush(ReadFuture future) throws IOException {

		if (future == null) {
			throw new IllegalStateException("null future");
		}

		if (future.flushed()) {
			throw new IllegalStateException("flushed already");
		}
		
		TCPEndPoint endPoint = this.endPoint;

		if (!endPoint.isOpened()) {

			throw new DisconnectException("disconnected");
		}

		IOWriteFuture writeFuture = null;

		try {
			
			ProtocolEncoder encoder = endPoint.getProtocolEncoder();
			
			IOReadFuture ioReadFuture = (IOReadFuture) future;

			writeFuture = encoder.encode(endPoint, ioReadFuture);

			writeFuture.attach(future.attachment());
			
			ioReadFuture.flush();
			
			endPoint.offer(writeFuture);

		} catch (IOException e) {

			logger.debug(e.getMessage(), e);

			IOEventHandle handle = future.getIOEventHandle();

			handle.exceptionCaught(this, future, e,IOEventState.WRITE);
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
		return endPoint.getLocalAddr();
	}

	public String getLocalHost() {
		return endPoint.getLocalHost();
	}

	public int getLocalPort() {
		return endPoint.getLocalPort();
	}

	public InetSocketAddress getLocalSocketAddress() {
		return endPoint.getLocalSocketAddress();
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

	public InetSocketAddress getRemoteSocketAddress() {
		return endPoint.getRemoteSocketAddress();
	}

	public Integer getSessionID() {
		return sessionID;
	}

	public TCPEndPoint getTCPEndPoint() {
		return endPoint;
	}

	public UDPEndPoint getUDPEndPoint() {
		return udpEndPoint;
	}

	public boolean isBlocking() {
		return endPoint.isBlocking();
	}

	public boolean isOpened() {
		return endPoint.isOpened();
	}

	public void removeAttribute(Object key) {
		attributes.remove(key);
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

	public void setUDPEndPoint(UDPEndPoint udpEndPoint) {

		if (this.udpEndPoint != null && this.udpEndPoint != udpEndPoint) {
			throw new IllegalArgumentException("udpEndPoint setted");
		}

		this.udpEndPoint = udpEndPoint;
	}
	
	public void active() {
		this.lastAccess = System.currentTimeMillis();
	}

	public long getLastAccessTime() {
		return lastAccess;
	}

	public String toString() {
		return endPoint.toString();
	}
}
