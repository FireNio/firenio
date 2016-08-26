package com.gifisan.nio.component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;

import com.gifisan.nio.DisconnectException;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.IOEventHandle.IOEventState;
import com.gifisan.nio.component.concurrent.ReentrantMap;
import com.gifisan.nio.component.protocol.IOReadFuture;
import com.gifisan.nio.component.protocol.IOWriteFuture;
import com.gifisan.nio.component.protocol.ProtocolEncoder;
import com.gifisan.nio.component.protocol.ReadFuture;
import com.gifisan.nio.extend.PluginContext;

//FIXME attributes
public class IOSession implements Session {

	private static final Logger			logger		= LoggerFactory.getLogger(IOSession.class);
	
	private Object						attachment;
	private boolean					closed;
	private NIOContext					context;
	private TCPEndPoint					endPoint;
	private Integer					sessionID;
	private UDPEndPoint					udpEndPoint;
	private Object[]					attachments	= new Object[4];
	private long						creationTime	= System.currentTimeMillis();
	private long						lastAccess;
	//FIXME 这里使用ReentrantMap有问题
	private ReentrantMap<Object, Object>	attributes	= new ReentrantMap<Object, Object>();

	public IOSession(TCPEndPoint endPoint, Integer sessionID) {
		this.context = endPoint.getContext();
		this.endPoint = endPoint;
		this.sessionID = sessionID;
		//这里认为在第一次Idle之前，连接都是畅通的
		this.lastAccess = this.creationTime;
	}

	public void clearAttributes() {
		attributes.clear();
	}

	public boolean closed() {
		return closed;
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
				} catch (Exception e) {
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

			IOReadFuture ioReadFuture = (IOReadFuture)future;
			
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

	public Object getAttachment(PluginContext context) {

		if (context == null) {
			throw new IllegalArgumentException("null context");
		}

		return attachments[context.getPluginIndex()];
	}

	public Object getAttribute(Object key) {
		return attributes.get(key);
	}

	public ReentrantMap<Object, Object> getAttributes() {
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

	public void setAttachment(PluginContext context, Object attachment) {

		if (context == null) {
			throw new IllegalArgumentException("null context");
		}

		this.attachments[context.getPluginIndex()] = attachment;
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
