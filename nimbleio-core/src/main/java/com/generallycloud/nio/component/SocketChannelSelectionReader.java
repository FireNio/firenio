package com.generallycloud.nio.component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.concurrent.atomic.AtomicLong;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.protocol.IOReadFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;

public class SocketChannelSelectionReader implements SelectionAcceptor {

	private IOReadFutureAcceptor	ioReadFutureAcceptor;
	
	private AtomicLong logID = new AtomicLong();
	
	private Logger logger = LoggerFactory.getLogger(SocketChannelSelectionReader.class);

	public SocketChannelSelectionReader(NIOContext context) {
		this.ioReadFutureAcceptor = context.getIOReadFutureAcceptor();
//		this.buffer = ByteBuffer.allocateDirect(1024 * 10);
		this.buffer = ByteBuffer.allocate(64);
	}

	private ByteBuffer	buffer;

	public void accept(SelectionKey selectionKey) throws Exception {

		SocketChannel channel = (SocketChannel) selectionKey.attachment();

		if (channel == null || !channel.isOpened()) {
			// 该channel已经被关闭
			return;
		}

		ByteBuffer buffer = this.buffer;

		buffer.clear();
		
		int length = channel.read(buffer);

//		logger.info("buffer read ==================logID:,{}==={}",logID.getAndIncrement(),length);
		
		if (length == -1) {
			CloseUtil.close(channel);
			return;
		}
		
		buffer.flip();

		IOSession session = channel.getSession();

		session.active();
		
		for (;;) {
			
			IOReadFuture future = channel.getReadFuture();

			if (future == null) {

				ProtocolDecoder decoder = channel.getProtocolDecoder();

				future = decoder.decode(session, buffer);

				if (future == null) {
					CloseUtil.close(channel);
					return;
				}

				channel.setReadFuture(future);
			}

			try {

				if (!future.read(session, buffer)) {

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

			ioReadFutureAcceptor.accept(session, future);

			if (!buffer.hasRemaining()) {
				return;
			}else{
//				logger.info("buffer remaining ==================logID:,{}==={}",logID.getAndIncrement(),buffer.remaining());
			}
		}

	}

}
