package com.generallycloud.nio.component;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;

public class TransparentByteBufReader extends LinkableChannelByteBufReader {
	
	private ReadFutureAcceptor readFutureAcceptor;
	
	public TransparentByteBufReader(BaseContext context) {
		this.readFutureAcceptor = context.getReadFutureAcceptor();
	}

	public void accept(SocketChannel channel, ByteBuf buf) throws Exception {

		UnsafeSession session = channel.getSession();

		for (;;) {

			if (!buf.hasRemaining()) {
				return;
			}

			ChannelReadFuture future = channel.getReadFuture();

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

			readFutureAcceptor.accept(session, future);
		}
	}
}
