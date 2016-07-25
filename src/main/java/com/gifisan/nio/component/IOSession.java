package com.gifisan.nio.component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;

import com.gifisan.nio.DisconnectException;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.MessageFormatter;
import com.gifisan.nio.component.concurrent.ReentrantMap;
import com.gifisan.nio.component.protocol.ProtocolEncoder;
import com.gifisan.nio.component.protocol.future.EmptyWriteFuture;
import com.gifisan.nio.component.protocol.future.IOReadFuture;
import com.gifisan.nio.component.protocol.future.IOWriteFuture;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.extend.PluginContext;

//FIXME attributes
public class IOSession implements Session {

	private static final Logger			logger		= LoggerFactory.getLogger(IOSession.class);
	
	private Object						attachment;
	private boolean					closed;
	private NIOContext					context;
	private ProtocolEncoder				encoder;
	private TCPEndPoint					endPoint;
	private EndPointWriter				endPointWriter;
	private String						machineType;
	private String 					session_description;
	private Integer					sessionID;
	private UDPEndPoint					udpEndPoint;
	private Object[]					attachments	= new Object[4];
	private long						creationTime	= System.currentTimeMillis();
	//FIXME 这里使用ReentrantMap有问题
	private ReentrantMap<Object, Object>	attributes	= new ReentrantMap<Object, Object>();

	public IOSession(TCPEndPoint endPoint, Integer sessionID) {
		this.context = endPoint.getContext();
		this.endPointWriter = endPoint.getEndPointWriter();
		this.encoder = context.getProtocolEncoder();
		this.endPoint = endPoint;
		this.sessionID = sessionID;
	}

	public void clearAttributes() {
		attributes.clear();
	}

	public boolean closed() {
		return closed;
	}

	public void destroy() {
		// FIXME
		CloseUtil.close(udpEndPoint);

		this.closed = true;

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

	public void disconnect() {
		//FIXME 可否X秒之后关闭
		this.endPoint.endConnect();
		this.endPoint.getEndPointWriter().offer(new EmptyWriteFuture(endPoint));
	}

	public void flush(ReadFuture future) {

		if (future.flushed()) {
			throw new IllegalStateException("flushed already");
		}

		if (!endPoint.isOpened()) {

			IOEventHandle handle = future.getIOEventHandle();

			if (handle != null) {

				handle.exceptionCaughtOnWrite(this, future, null, new DisconnectException("disconnected"));
			}
			return;
		}

		IOWriteFuture writeFuture = null;

		try {

			writeFuture = encoder.encode(endPoint, future);

			((IOReadFuture) future).flush();

			writeFuture.attach(future.attachment());

			this.endPointWriter.offer(writeFuture);

		} catch (IOException e) {

			logger.debug(e.getMessage(), e);

			IOEventHandle handle = future.getIOEventHandle();

			handle.exceptionCaughtOnWrite(this, future, writeFuture, e);
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

	public String getMachineType() {
		return machineType;
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

	public void setMachineType(String machineType) {
		this.machineType = machineType;
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
	
	public String toString() {
		
		if (session_description == null) {
			session_description = MessageFormatter.format("[Session@edp{}]", endPoint);
		}
		
		return session_description;
	}
}
