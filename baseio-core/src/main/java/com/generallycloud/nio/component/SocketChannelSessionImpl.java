package com.generallycloud.nio.component;

import javax.net.ssl.SSLEngine;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.common.ssl.SslHandler;
import com.generallycloud.nio.component.IOEventHandle.IOEventState;
import com.generallycloud.nio.component.concurrent.EventLoop;
import com.generallycloud.nio.component.concurrent.Waiter;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;
import com.generallycloud.nio.protocol.ReadFuture;

public abstract class SocketChannelSessionImpl extends SessionImpl implements SocketSession {

	private static final Logger	logger	= LoggerFactory.getLogger(SocketChannelSessionImpl.class);

	protected Waiter<Exception>	handshakeWaiter;
	protected SSLEngine			sslEngine;
	protected SslHandler		sslHandler;

	public SocketChannelSessionImpl(SocketChannel channel,EventLoop eventLoop, Integer sessionID) {
		super(channel,eventLoop, sessionID);
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

			IOEventHandle handle = readFuture.getIOEventHandle();

			handle.exceptionCaught(this, readFuture, e, IOEventState.WRITE);
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
