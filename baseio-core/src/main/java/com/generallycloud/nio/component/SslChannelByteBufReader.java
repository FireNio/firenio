package com.generallycloud.nio.component;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.protocol.SslReadFuture;
import com.generallycloud.nio.protocol.SslReadFutureImpl;

public class SslChannelByteBufReader extends LinkableChannelByteBufReader {

	public void accept(SocketChannel channel, ByteBuf buffer) throws Exception {

		UnsafeSession session = channel.getSession();

		for (;;) {

			if (!buffer.hasRemaining()) {
				return;
			}

			SslReadFuture future = channel.getSslReadFuture();

			if (future == null) {

				ByteBuf buf = allocate(session,SslReadFuture.SSL_RECORD_HEADER_LENGTH);

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

			ByteBuf produce = future.getProduce();

			if (produce == null) {
				continue;
			}

			try {

				nextAccept(channel, produce);

			} finally {

				ReleaseUtil.release(future);
			}
		}
	}

}
