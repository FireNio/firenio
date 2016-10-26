package com.generallycloud.nio.protocol;

import java.io.IOException;

import javax.net.ssl.SSLEngine;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.common.ssl.SslHandler;
import com.generallycloud.nio.component.IOEventHandle;
import com.generallycloud.nio.component.IOEventHandle.IOEventState;
import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.component.SocketChannel;

public class IOWriteFutureImpl extends FutureImpl implements IOWriteFuture {

	protected ReadFuture		readFuture;
	protected ByteBuf			buf;
	protected IOWriteFuture		next;
	protected boolean			wrap;

	private static final Logger	logger	= LoggerFactory.getLogger(IOWriteFutureImpl.class);

	public IOWriteFutureImpl(ReadFuture readFuture, ByteBuf buf) {
		this(readFuture, buf, true);
	}

	public IOWriteFutureImpl(ReadFuture readFuture, ByteBuf buf, boolean wrap) {
		this.readFuture = readFuture;
		this.buf = buf;
		this.wrap = wrap;
	}

	public void onException(IOSession session, Exception e) {

		ReadFuture readFuture = this.getReadFuture();

		ReleaseUtil.release(this);

		IOEventHandle handle = readFuture.getIOEventHandle();

		try {
			handle.exceptionCaught(session, readFuture, e, IOEventState.WRITE);
		} catch (Throwable e1) {
			logger.debug(e1.getMessage(), e1);
		}
	}

	public void onSuccess(IOSession session) {

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

		ByteBuf buffer = this.buf;

		buffer.write(channel);

		return !buffer.hasRemaining();
	}

	public ReadFuture getReadFuture() {
		return readFuture;
	}

	public void setReadFuture(ReadFuture readFuture) {
		this.readFuture = readFuture;
	}

	public String toString() {
		return readFuture.getWriteBuffer().toString();
	}

	public void release() {
		ReleaseUtil.release(buf);
	}

	public IOWriteFuture duplicate() {
		return new IOWriteFutureImpl(readFuture, buf.duplicate());
	}

	public IOWriteFuture getNext() {
		return next;
	}

	public void setNext(IOWriteFuture future) {
		this.next = future;
	}

	public void wrapSSL(SSLEngine engine, SslHandler handler) throws IOException {

		if (wrap) {

			ByteBuf old = this.buf;

			try {
				this.buf = handler.wrap(engine, buf);
			} finally {
				ReleaseUtil.release(old);
			}
		}
	}

}
