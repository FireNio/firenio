package com.generallycloud.nio.component;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.protocol.SslReadFuture;
import com.generallycloud.nio.protocol.SslReadFutureImpl;

public class SslSocketChannelSelectionReader extends SocketChannelSelectionReader{

	public SslSocketChannelSelectionReader(BaseContext context) {
		super(context);
	}
	
	protected void accept(SocketChannel channel,UnsafeSession session, ByteBuf buffer) throws Exception {
		
		for (;;) {

			if (!buffer.hasRemaining()) {
				return;
			}

			SslReadFuture future = channel.getSslReadFuture();

			if (future == null) {

				ByteBuf buf = byteBufferPool.allocate(SslReadFuture.SSL_RECORD_HEADER_LENGTH);

				future = new SslReadFutureImpl(session, buf);

				channel.setSslReadFuture(future);
			}

			try {

				if (!future.read(session, buffer)) {

					return;
				}

			} catch (Throwable e) {

				channel.setSslReadFuture(null);

				ReleaseUtil.release(future);

				if (e instanceof IOException) {
					throw (IOException) e;
				}

				throw new IOException("exception occurred when read from channel,the nested exception is,"
						+ e.getMessage(), e);
			}

			channel.setSslReadFuture(null);

			// FIXME 不友好
			ByteBuf product = future.getProduct();

			if (product == null) {
				continue;
			}

			try {

				super.accept(channel, session, product);

			} finally {

				ReleaseUtil.release(future);
			}
		}
	}
}
