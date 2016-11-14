package com.generallycloud.nio.protocol;

import java.io.IOException;

import com.generallycloud.nio.Linkable;
import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.common.ssl.SslHandler;
import com.generallycloud.nio.component.IOEventHandle;
import com.generallycloud.nio.component.IOEventHandle.IOEventState;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.component.SocketChannel;

public class ChannelWriteFutureImpl extends FutureImpl implements ChannelWriteFuture {

	protected ReadFuture				readFuture;
	protected ByteBuf					buf;
	protected Linkable<ChannelWriteFuture>	next;

	private static final Logger			logger	= LoggerFactory.getLogger(ChannelWriteFutureImpl.class);

	public ChannelWriteFutureImpl(ReadFuture readFuture, ByteBuf buf) {
		this.readFuture = readFuture;
		this.buf = buf;
		this.buf.nioBuffer();
	}

	public void onException(SocketSession session, Exception e) {

		ReadFuture readFuture = this.getReadFuture();

		ReleaseUtil.release(this);

		IOEventHandle handle = readFuture.getIOEventHandle();

		try {
			handle.exceptionCaught(session, readFuture, e, IOEventState.WRITE);
		} catch (Throwable e1) {
			logger.debug(e1.getMessage(), e1);
		}
	}

	public void onSuccess(SocketSession session) {

		ReadFuture readFuture = this.getReadFuture();

		ReleaseUtil.release(this);

		IOEventHandle handle = readFuture.getIOEventHandle();

		try {
			handle.futureSent(session, readFuture);
		} catch (Throwable e) {
			logger.debug(e);
		}
	}

	public boolean write(SocketChannel channel) throws IOException {

		ByteBuf buf = this.buf;

		buf.write(channel);

		return !buf.hasRemaining();
	}

	public ReadFuture getReadFuture() {
		return readFuture;
	}

	public void setReadFuture(ReadFuture readFuture) {
		this.readFuture = readFuture;
	}

	public String toString() {
		return readFuture.toString();
	}

	public void release() {
		ReleaseUtil.release(buf);
	}

	public ChannelWriteFuture duplicate() {
		return new ChannelWriteFutureImpl(readFuture, buf.duplicate());
	}

	public Linkable<ChannelWriteFuture> getNext() {
		return next;
	}

	public void setNext(Linkable<ChannelWriteFuture> next) {
		this.next = next;
	}

	public ChannelWriteFuture getValue() {
		return this;
	}

	public void wrapSSL(SocketSession session, SslHandler handler) throws IOException {

		ByteBuf old = this.buf;

		try {

			ByteBuf _buf = handler.wrap(session, old);

			if (_buf == null) {
				throw new IOException("closed ssl");
			}

			this.buf = _buf;

			this.buf.nioBuffer();

		} finally {
			ReleaseUtil.release(old);
		}
	}
}
