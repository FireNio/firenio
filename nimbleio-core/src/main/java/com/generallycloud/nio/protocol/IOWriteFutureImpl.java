package com.generallycloud.nio.protocol;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.IOEventHandle;
import com.generallycloud.nio.component.IOEventHandle.IOEventState;
import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.SocketChannel;

public class IOWriteFutureImpl extends FutureImpl implements IOWriteFuture {

	protected Session			session;
	protected ReadFuture		readFuture;
	protected SocketChannel		channel;
	protected ByteBuf			buffer;
	protected IOWriteFuture		next;
	
	private static final Logger	logger	= LoggerFactory.getLogger(IOWriteFutureImpl.class);

	public IOWriteFutureImpl(SocketChannel channel, ReadFuture readFuture, ByteBuf buffer) {
		this.channel = channel;
		this.readFuture = readFuture;
		this.session = channel.getSession();
		this.buffer = buffer;
	}

	protected void updateNetworkState(int length) {

		channel.updateNetworkState(length);
	}

	public void onException(Exception e) {

		ReadFuture readFuture = this.getReadFuture();

		ReleaseUtil.release(this);

		IOEventHandle handle = readFuture.getIOEventHandle();

		if (handle == null) {
			handle = session.getContext().getIOEventHandleAdaptor();
		}

		try {
			handle.exceptionCaught(session, readFuture, e, IOEventState.WRITE);
		} catch (Throwable e1) {
			logger.debug(e1.getMessage(),e1);
		}
	}

	public void onSuccess() {

		ReadFuture readFuture = this.getReadFuture();

		ReleaseUtil.release(this);

		IOEventHandle handle = readFuture.getIOEventHandle();

		if (handle == null) {
			handle = session.getContext().getIOEventHandleAdaptor();
		}

		try {
			handle.futureSent(session, readFuture);
		} catch (Throwable e) {
			logger.debug(e);
		}
	}
	
	public boolean write() throws IOException {

		ByteBuf buffer = this.buffer;

		updateNetworkState(buffer.write(channel));

		return !buffer.hasRemaining();
	}

	public SocketChannel getSocketChannel() {
		return channel;
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
		ReleaseUtil.release(buffer);
	}

	public IOWriteFuture duplicate(IOSession session) {
		return new IOWriteFutureImpl(session.getSocketChannel(), readFuture, buffer.duplicate());
	}

	public IOWriteFuture getNext() {
		return next;
	}

	public void setNext(IOWriteFuture future) {
		this.next = future;
	}
	
}
