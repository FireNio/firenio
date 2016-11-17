package com.generallycloud.nio.component;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.SSLException;

import com.generallycloud.nio.Linkable;
import com.generallycloud.nio.buffer.EmptyByteBuf;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.connector.ChannelConnector;
import com.generallycloud.nio.protocol.EmptyReadFuture;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ChannelWriteFutureImpl;
import com.generallycloud.nio.protocol.ReadFuture;

public class UnsafeSessionImpl extends SocketChannelSessionImpl implements UnsafeSession {

	private static final Logger	logger	= LoggerFactory.getLogger(UnsafeSessionImpl.class);

	public UnsafeSessionImpl(SocketChannel channel, Integer sessionID) {
		super(channel, sessionID);
	}

	public SocketChannel getSocketChannel() {
		return channel;
	}

	public void close() {

		ReentrantLock lock = channel.getChannelLock();
		
		lock.lock();
		
		try{
			
			if (isClosed()) {
				return;
			}

			doClose();
			
		}finally{
			
			lock.unlock();
		}
		
		fireClosed();
	}

	private void doClose() {
		
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
				// ignore
				// logger.error(e.getMessage(), e);
			}
		}

		physicalClose(channel);

		ChannelService service = context.getChannelService();
		
		if (service instanceof ChannelConnector) {
			
			try {
				((ChannelConnector) service).physicalClose();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		
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
