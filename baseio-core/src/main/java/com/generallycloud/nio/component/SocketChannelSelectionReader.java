package com.generallycloud.nio.component;

import java.io.IOException;
import java.nio.channels.SelectionKey;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.ByteBufferPool;
import com.generallycloud.nio.buffer.UnpooledMemoryPool;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.IOEventHandle.IOEventState;
import com.generallycloud.nio.component.concurrent.EventLoop;
import com.generallycloud.nio.protocol.IOReadFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ReadFuture;

public class SocketChannelSelectionReader implements SelectionAcceptor {

	protected ByteBuf		buf		= null;

	protected ByteBufferPool	byteBufferPool	= null;
	
	private Logger				logger		= LoggerFactory.getLogger(SocketChannelSelectionReader.class);

	public SocketChannelSelectionReader(BaseContext context) {
		int readBuffer = context.getServerConfiguration().getSERVER_READ_BUFFER();
		this.byteBufferPool = context.getHeapByteBufferPool();
		this.buf = UnpooledMemoryPool.allocate(readBuffer);// FIXME 使用direct
		// this.buffer = ByteBuffer.allocateDirect(readBuffer);
	}

	public void accept(SelectionKey selectionKey) throws Exception {

		SocketChannel channel = (SocketChannel) selectionKey.attachment();

		if (channel == null || !channel.isOpened()) {
			// 该channel已经被关闭
			return;
		}

		ByteBuf buf = this.buf;

		buf.clear();
		
		buf.nioBuffer();

		int length = buf.read(channel);

		if (length == -1) {
			CloseUtil.close(channel);
			return;
		}

		buf.flip();
		
		UnsafeSession session = channel.getSession();

		session.active();

		accept(channel, session, buf);
		
	}

	private void accept(final Session session, final IOReadFuture future) throws Exception {

		if (future.isSilent()) {
			return;
		}

		if (future.isHeartbeat()) {

			acceptHeartBeat(session, future);

			return;
		}

		EventLoop eventLoop = session.getEventLoop();

		eventLoop.dispatch(new Runnable() {

			public void run() {

				BaseContext context = session.getContext();

				IOEventHandle eventHandle = context.getIOEventHandleAdaptor();

				try {

					eventHandle.accept(session, future);

				} catch (Exception e) {

					logger.error(e.getMessage(), e);

					eventHandle.exceptionCaught(session, future, e, IOEventState.HANDLE);
				}
			}
		});
	}

	protected void accept(SocketChannel channel, UnsafeSession session, ByteBuf buf) throws Exception {

		for (;;) {

			if (!buf.hasRemaining()) {
				return;
			}

			IOReadFuture future = channel.getReadFuture();

			if (future == null) {

				ProtocolDecoder decoder = channel.getProtocolDecoder();

				future = decoder.decode(session, buf);

				if (future == null) {
					CloseUtil.close(channel);
					return;
				}

				channel.setReadFuture(future);
			}

			try {

				if (!future.read(session, buf)) {

					return;
				}

				ReleaseUtil.release(future);

			} catch (Throwable e) {

				ReleaseUtil.release(future);

				if (e instanceof IOException) {
					throw (IOException) e;
				}

				throw new IOException("exception occurred when read from channel,the nested exception is,"
						+ e.getMessage(), e);
			}

			channel.setReadFuture(null);

			accept(session, future);

		}
	}

	private void acceptHeartBeat(final Session session, final IOReadFuture future) {

		if (future.isPING()) {

			logger.info("收到心跳请求!来自：{}", session);

			BaseContext context = session.getContext();

			BeatFutureFactory factory = context.getBeatFutureFactory();

			if (factory == null) {

				RuntimeException e = new RuntimeException("none factory of BeatFuture");

				CloseUtil.close(session);

				logger.error(e.getMessage(), e);

				return;
			}

			ReadFuture f = factory.createPONGPacket(session);

			try {
				session.flush(f);
			} catch (IOException e) {
				CloseUtil.close(session);
				logger.error(e.getMessage(), e);
				return;
			}
		} else {
			logger.info("收到心跳回报!来自：{}", session);
		}

	}
}
