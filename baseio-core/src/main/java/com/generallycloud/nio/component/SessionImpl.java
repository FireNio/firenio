package com.generallycloud.nio.component;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.HashMap;

import com.generallycloud.nio.DisconnectException;
import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.IOEventHandle.IOEventState;
import com.generallycloud.nio.component.concurrent.EventLoop;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ReadFuture;

public abstract class SessionImpl implements Session {

	private static final Logger		logger		= LoggerFactory.getLogger(SessionImpl.class);

	protected Object					attachment;
	protected Object[]					attachments;
	protected BaseContext				context;
	protected Integer					sessionID;
	protected EventLoop				eventLoop;
	protected SocketChannel				channel;
	protected HashMap<Object, Object>		attributes	= new HashMap<Object, Object>();

	public SessionImpl(SocketChannel channel,EventLoop eventLoop,Integer sessionID) {
		this.context = channel.getContext();
		this.channel = channel;
		this.sessionID = sessionID;
		this.attachments = new Object[context.getSessionAttachmentSize()];
		this.eventLoop = eventLoop;
	}

	public void active() {
		channel.active();
	}

	public void clearAttributes() {
		attributes.clear();
	}

	public void flush(ReadFuture future) {

		if (future == null || future.flushed()) {
			return;
		}

		SocketChannel socketChannel = this.channel;

		if (!socketChannel.isOpened()) {

			IOEventHandle handle = future.getIOEventHandle();
			
			exceptionCaught(handle, future, new DisconnectException("disconnected"), IOEventState.WRITE);

			return;
		}

		ChannelWriteFuture writeFuture = null;

		try {

			ProtocolEncoder encoder = socketChannel.getProtocolEncoder();

			ChannelReadFuture ioReadFuture = (ChannelReadFuture) future;

			writeFuture = encoder.encode(getByteBufAllocator(), ioReadFuture);

			ioReadFuture.flush();

			flush(writeFuture);

		} catch (Exception e) {

			ReleaseUtil.release(writeFuture);

			logger.debug(e.getMessage(), e);

			IOEventHandle handle = future.getIOEventHandle();

			exceptionCaught(handle, future, e, IOEventState.WRITE);
		}
	}
	
	private void exceptionCaught(IOEventHandle handle,ReadFuture future, Exception cause, IOEventState state){
		try {
			handle.exceptionCaught(this, future, cause, state);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
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
		return channel.getCreationTime();
	}

	public Charset getEncoding() {
		return context.getEncoding();
	}

	public EventLoop getEventLoop() {
		return eventLoop;
	}

	public long getLastAccessTime() {
		return channel.getLastAccessTime();
	}

	public String getLocalAddr() {
		return channel.getLocalAddr();
	}

	public String getLocalHost() {
		return channel.getLocalHost();
	}

	public int getLocalPort() {
		return channel.getLocalPort();
	}

	public InetSocketAddress getLocalSocketAddress() {
		return channel.getLocalSocketAddress();
	}

	public int getMaxIdleTime() throws SocketException {
		return channel.getMaxIdleTime();
	}

	public ProtocolEncoder getProtocolEncoder() {
		return channel.getProtocolEncoder();
	}

	public String getProtocolID() {
		return channel.getProtocolFactory().getProtocolID();
	}

	public String getRemoteAddr() {
		return channel.getRemoteAddr();
	}

	public String getRemoteHost() {
		return channel.getRemoteHost();
	}

	public int getRemotePort() {
		return channel.getRemotePort();
	}

	public InetSocketAddress getRemoteSocketAddress() {
		return channel.getRemoteSocketAddress();
	}

	public Integer getSessionID() {
		return sessionID;
	}

	public boolean isBlocking() {
		return channel.isBlocking();
	}

	public boolean isClosed() {
		return !isOpened();
	}

	public boolean isOpened() {
		return channel.isOpened();
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

	public void setSessionID(Integer sessionID) {
		this.sessionID = sessionID;
	}

	public String toString() {
		return channel.toString();
	}

	public ByteBufAllocator getByteBufAllocator() {
		return channel.getByteBufAllocator();
	}

	public boolean isInSelectorLoop() {
		return channel.isInSelectorLoop();
	}
	
	public void close() {
		CloseUtil.close(channel);
	}
	
}
