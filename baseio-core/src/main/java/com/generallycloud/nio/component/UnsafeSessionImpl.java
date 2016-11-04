package com.generallycloud.nio.component;

import javax.net.ssl.SSLException;

import com.generallycloud.nio.Linkable;
import com.generallycloud.nio.buffer.EmptyMemoryBlock;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.protocol.EmptyReadFuture;
import com.generallycloud.nio.protocol.IOWriteFuture;
import com.generallycloud.nio.protocol.IOWriteFutureImpl;
import com.generallycloud.nio.protocol.ReadFuture;

public class UnsafeSessionImpl extends SocketChannelSessionImpl implements UnsafeSession {

	private static final Logger	logger	= LoggerFactory.getLogger(UnsafeSessionImpl.class);

	public UnsafeSessionImpl(SocketChannel channel, Integer sessionID) {
		super(channel, sessionID);
	}

	public SocketChannel getSocketChannel() {
		return socketChannel;
	}

	public void close() {

		synchronized (this) {

			if (isClosed()) {
				return;
			}

			doClose();
		}

		fireClosed();
	}

	private void doClose() {

		if (isEnableSSL()) {

			sslEngine.closeOutbound();

			if (context.getSslContext().isClient()) {

				ReadFuture future = EmptyReadFuture.getEmptyReadFuture(context);

				IOWriteFuture f = new IOWriteFutureImpl(future, EmptyMemoryBlock.EMPTY_BYTEBUF);

				flush(f);
			}

			try {
				sslEngine.closeInbound();
			} catch (SSLException e) {
				// ignore
				// logger.error(e.getMessage(), e);
			}

		}

		physicalClose(datagramChannel);

		physicalClose(socketChannel);
	}

	private void physicalClose(Channel channel) {

		if (channel == null) {
			return;
		}

		try {
			channel.physicalClose();
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void fireClosed() {

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

}
