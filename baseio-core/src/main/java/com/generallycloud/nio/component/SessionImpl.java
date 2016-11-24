package com.generallycloud.nio.component;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.HashMap;

import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.component.concurrent.EventLoop;

public abstract class SessionImpl implements Session {

	protected Object					attachment;
	protected Object[]					attachments;
	protected BaseContext				context;
	protected Integer					sessionID;
	protected EventLoop				eventLoop;
	protected HashMap<Object, Object>		attributes	= new HashMap<Object, Object>();

	public SessionImpl(BaseContext context,Integer sessionID) {
		this.context = context;
		this.sessionID = sessionID;
		this.eventLoop = context.getEventLoopGroup().getNext();
	}
	
	protected abstract Channel getChannel();

	public void active() {
		getChannel().active();
	}

	public void clearAttributes() {
		attributes.clear();
	}

	public Object getAttachment() {
		return attachment;
	}

	public Object getAttachment(int index) {
		if (attachments == null) {
			return null;
		}
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
		return getChannel().getCreationTime();
	}

	public Charset getEncoding() {
		return context.getEncoding();
	}

	public EventLoop getEventLoop() {
		return eventLoop;
	}

	public long getLastAccessTime() {
		return getChannel().getLastAccessTime();
	}

	public String getLocalAddr() {
		return getChannel().getLocalAddr();
	}

	public String getLocalHost() {
		return getChannel().getLocalHost();
	}

	public int getLocalPort() {
		return getChannel().getLocalPort();
	}

	public InetSocketAddress getLocalSocketAddress() {
		return getChannel().getLocalSocketAddress();
	}

	public int getMaxIdleTime() throws SocketException {
		return getChannel().getMaxIdleTime();
	}

	public String getRemoteAddr() {
		return getChannel().getRemoteAddr();
	}

	public String getRemoteHost() {
		return getChannel().getRemoteHost();
	}

	public int getRemotePort() {
		return getChannel().getRemotePort();
	}

	public InetSocketAddress getRemoteSocketAddress() {
		return getChannel().getRemoteSocketAddress();
	}

	public Integer getSessionID() {
		return sessionID;
	}

	public boolean isClosed() {
		return !isOpened();
	}

	public boolean isOpened() {
		return getChannel().isOpened();
	}

	public Object removeAttribute(Object key) {
		return attributes.remove(key);
	}

	public void setAttachment(int index, Object attachment) {
		if (attachments == null) {
			attachments = new Object[context.getSessionAttachmentSize()];
		}
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
		return getChannel().toString();
	}

	public ByteBufAllocator getByteBufAllocator() {
		return getChannel().getByteBufAllocator();
	}

	public boolean isInSelectorLoop() {
		return getChannel().isInSelectorLoop();
	}
	
	public void close() {
		CloseUtil.close(getChannel());
	}
	
}
