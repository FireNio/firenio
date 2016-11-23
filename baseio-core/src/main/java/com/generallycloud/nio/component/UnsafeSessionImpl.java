package com.generallycloud.nio.component;

import javax.net.ssl.SSLException;

import com.generallycloud.nio.Linkable;
import com.generallycloud.nio.buffer.EmptyByteBuf;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.concurrent.Waiter;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ChannelWriteFutureImpl;
import com.generallycloud.nio.protocol.EmptyReadFuture;
import com.generallycloud.nio.protocol.ReadFuture;

public class UnsafeSessionImpl extends SocketChannelSessionImpl implements UnsafeSession {

	private static final Logger	logger	= LoggerFactory.getLogger(UnsafeSessionImpl.class);

	public UnsafeSessionImpl(SocketChannel channel,Integer sessionID) {
		super(channel,sessionID);
	}

	public SocketChannel getSocketChannel() {
		return channel;
	}

	public void fireOpend() {

		if (isEnableSSL() && context.getSslContext().isClient()) {

			handshakeWaiter = new Waiter<Exception>();

			ReadFuture future = EmptyReadFuture.getEmptyReadFuture(context);

			ChannelWriteFuture f = new ChannelWriteFutureImpl(future, EmptyByteBuf.EMPTY_BYTEBUF);

			flush(f);

			// wait

			if (handshakeWaiter.await(3000000)) {// FIXME test
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

	public void fireClosed() {

		Linkable<SessionEventListener> linkable = context.getSessionEventListenerLink();

		for (; linkable != null;) {

			try {

				linkable.getValue().sessionClosed(this);

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			linkable = linkable.getNext();
		}
	}

	public void physicalClose() {

		if (isEnableSSL()) {

			sslEngine.closeOutbound();

			if (context.getSslContext().isClient()) {

				ReadFuture future = EmptyReadFuture.getEmptyReadFuture(context);

				ChannelWriteFuture f = new ChannelWriteFutureImpl(future, EmptyByteBuf.EMPTY_BYTEBUF);

				flush(f);
			}

			try {
				sslEngine.closeInbound();
			} catch (SSLException e) {
			}
		}

		fireClosed();

	}

}
