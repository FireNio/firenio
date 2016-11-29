package com.generallycloud.nio.component;

import javax.net.ssl.SSLEngine;

import com.generallycloud.nio.DisconnectException;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.IoEventHandle.IoEventState;
import com.generallycloud.nio.component.concurrent.EventLoop;
import com.generallycloud.nio.component.concurrent.Waiter;
import com.generallycloud.nio.component.ssl.SslHandler;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;
import com.generallycloud.nio.protocol.ReadFuture;

public abstract class SocketChannelSessionImpl extends SessionImpl implements SocketSession {

	private static final Logger		logger	= LoggerFactory.getLogger(SocketChannelSessionImpl.class);

	protected Waiter<Exception>		handshakeWaiter;
	protected SSLEngine			sslEngine;
	protected SslHandler			sslHandler;
	protected SocketChannel			channel;
	protected SocketChannelContext	context;

	public SocketChannelSessionImpl(SocketChannel channel, Integer sessionID) {
		super(sessionID);
		this.channel = channel;
		this.context = channel.getContext();
		if (context.isEnableSSL()) {
			this.sslHandler = context.getSslContext().getSslHandler();
			this.sslEngine = context.getSslContext().newEngine();
		}
	}
	
	public SocketChannelContext getContext() {
		return context;
	}

	protected Channel getChannel() {
		return channel;
	}

	public ProtocolEncoder getProtocolEncoder() {
		return channel.getProtocolEncoder();
	}

	public String getProtocolID() {
		return channel.getProtocolFactory().getProtocolID();
	}

	public void finishHandshake(Exception e) {

		if (context.getSslContext().isClient()) {
			this.handshakeWaiter.setPayload(e);
		}
	}
	
	public void setAttachment(int index, Object attachment) {
		if (attachments == null) {
			attachments = new Object[getContext().getSessionAttachmentSize()];
		}
		this.attachments[index] = attachment;
	}
	
	public Object getAttachment(int index) {
		if (attachments == null) {
			return null;
		}
		return attachments[index];
	}

	public boolean isEnableSSL() {
		return context.isEnableSSL();
	}

	private void exceptionCaught(IoEventHandle handle, ReadFuture future, Exception cause, IoEventState state) {
		try {
			handle.exceptionCaught(this, future, cause, state);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public boolean isBlocking() {
		return channel.isBlocking();
	}

	public void flush(ReadFuture future) {

		if (future == null || future.flushed()) {
			return;
		}

		SocketChannel socketChannel = this.channel;

		if (!socketChannel.isOpened()) {

			IoEventHandle handle = future.getIOEventHandle();

			exceptionCaught(handle, future, new DisconnectException("disconnected"), IoEventState.WRITE);

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

			IoEventHandle handle = future.getIOEventHandle();

			exceptionCaught(handle, future, e, IoEventState.WRITE);
		}
	}

	public void flush(ChannelWriteFuture future) {

		try {

			// FIXME 部分情况下可以不在业务线程做wrapssl
			if (isEnableSSL()) {
				future.wrapSSL(this, sslHandler);
			}

			channel.offer(future);

		} catch (Exception e) {

			ReleaseUtil.release(future);

			ReadFuture readFuture = future.getReadFuture();

			logger.debug(e.getMessage(), e);

			IoEventHandle handle = readFuture.getIOEventHandle();

			handle.exceptionCaught(this, readFuture, e, IoEventState.WRITE);
		}
	}

	public ProtocolDecoder getProtocolDecoder() {
		return channel.getProtocolDecoder();
	}

	public ProtocolFactory getProtocolFactory() {
		return channel.getProtocolFactory();
	}

	public SSLEngine getSSLEngine() {
		return sslEngine;
	}

	public EventLoop getEventLoop() {
		return channel.getEventLoop();
	}

	public SslHandler getSslHandler() {
		return context.getSslContext().getSslHandler();
	}

	public void setProtocolDecoder(ProtocolDecoder protocolDecoder) {
		channel.setProtocolDecoder(protocolDecoder);
	}

	public void setProtocolEncoder(ProtocolEncoder protocolEncoder) {
		channel.setProtocolEncoder(protocolEncoder);
	}

	public void setProtocolFactory(ProtocolFactory protocolFactory) {
		channel.setProtocolFactory(protocolFactory);
	}

}
