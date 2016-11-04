package com.generallycloud.nio.component;

import javax.net.ssl.SSLEngine;

import com.generallycloud.nio.Linkable;
import com.generallycloud.nio.buffer.v4.EmptyMemoryBlockV4;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.common.ssl.SslHandler;
import com.generallycloud.nio.component.IOEventHandle.IOEventState;
import com.generallycloud.nio.component.concurrent.Waiter;
import com.generallycloud.nio.protocol.EmptyReadFuture;
import com.generallycloud.nio.protocol.IOWriteFuture;
import com.generallycloud.nio.protocol.IOWriteFutureImpl;
import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;
import com.generallycloud.nio.protocol.ReadFuture;

public abstract class SocketChannelSessionImpl extends SessionImpl implements SocketSession {

	private static final Logger		logger		= LoggerFactory.getLogger(SocketChannelSessionImpl.class);

	protected Waiter<Exception>			handshakeWaiter;
	protected SSLEngine				sslEngine;
	protected SslHandler				sslHandler;

	public SocketChannelSessionImpl(SocketChannel channel, Integer sessionID) {
		super(channel, sessionID);
		if (context.isEnableSSL()) {
			this.sslHandler = context.getSslContext().getSslHandler();
			this.sslEngine = context.getSslContext().newEngine();
		}
	}

	public void finishHandshake(Exception e) {

		if (context.getSslContext().isClient()) {
			this.handshakeWaiter.setPayload(e);
		}
	}
	
	public boolean isEnableSSL() {
		return context.isEnableSSL();
	}

	public void fireOpend() {

		if (isEnableSSL() && context.getSslContext().isClient()) {

			handshakeWaiter = new Waiter<Exception>();

			ReadFuture future = EmptyReadFuture.getEmptyReadFuture(context);

			IOWriteFuture f = new IOWriteFutureImpl(future, EmptyMemoryBlockV4.EMPTY_BYTEBUF);

			flush(f);

			// wait

			if (handshakeWaiter.await(3000)) {
				CloseUtil.close(this);
				throw new RuntimeException("hands shake failed");
			}

			if (handshakeWaiter.getPayload() != null) {
				throw new RuntimeException(handshakeWaiter.getPayload());
			}
			// success
		}

		Linkable<SessionEventListener> linkable = context.getSessionEventListenerLink();

		for (; linkable != null;) {

			try {

				linkable.getValue().sessionOpened(this);

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			linkable = linkable.getNext();
		}
	}

	public void flush(IOWriteFuture future) {

		try {

			// FIXME 部分情况下可以不在业务线程做wrapssl
			if (isEnableSSL()) {
				future.wrapSSL(this, sslHandler);
			}

			socketChannel.offer(future);

		} catch (Exception e) {

			ReleaseUtil.release(future);

			ReadFuture readFuture = future.getReadFuture();

			logger.debug(e.getMessage(), e);

			IOEventHandle handle = readFuture.getIOEventHandle();

			handle.exceptionCaught(this, readFuture, e, IOEventState.WRITE);
		}
	}

	public ProtocolDecoder getProtocolDecoder() {
		return socketChannel.getProtocolDecoder();
	}

	public ProtocolFactory getProtocolFactory() {
		return socketChannel.getProtocolFactory();
	}

	public SSLEngine getSSLEngine() {
		return sslEngine;
	}

	public SslHandler getSslHandler() {
		return context.getSslContext().getSslHandler();
	}

	public void setProtocolDecoder(ProtocolDecoder protocolDecoder) {
		socketChannel.setProtocolDecoder(protocolDecoder);
	}

	public void setProtocolEncoder(ProtocolEncoder protocolEncoder) {
		socketChannel.setProtocolEncoder(protocolEncoder);
	}

	public void setProtocolFactory(ProtocolFactory protocolFactory) {
		socketChannel.setProtocolFactory(protocolFactory);
	}

}
