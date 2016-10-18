package com.generallycloud.nio.component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.protocol.IOReadFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;

public class SocketChannelSelectionReader implements SelectionAcceptor {

	private IOReadFutureAcceptor	ioReadFutureAcceptor;
	
	public SocketChannelSelectionReader(NIOContext context) {
		this.ioReadFutureAcceptor = context.getIOReadFutureAcceptor();
		int readBuffer = context.getServerConfiguration().getSERVER_READ_BUFFER();
//		this.buffer = ByteBuffer.allocateDirect(readBuffer);
		this.buffer = ByteBuffer.allocate(readBuffer);//FIXME 使用direct
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
			}
			
		}
	}
}
