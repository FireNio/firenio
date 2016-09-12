package com.generallycloud.nio.component.protocol;

import java.io.IOException;
import java.io.InputStream;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.IOEventHandle;
import com.generallycloud.nio.component.IOEventHandle.IOEventState;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.TCPEndPoint;

public class IOWriteFutureImpl extends FutureImpl implements IOWriteFuture {

	protected Session			session;
	protected ReadFuture		readFuture;
	protected TCPEndPoint		endPoint;
	protected ByteBuf			buffer;
	protected InputStream		inputStream;
	private static final Logger	logger	= LoggerFactory.getLogger(IOWriteFutureImpl.class);

	public IOWriteFutureImpl(TCPEndPoint endPoint, ReadFuture readFuture, ByteBuf buffer) {
		this.endPoint = endPoint;
		this.readFuture = readFuture;
		this.session = endPoint.getSession();
		this.buffer = buffer;
	}

	protected void updateNetworkState(int length) {

		endPoint.updateNetworkState(length);
	}

	public void onException(IOException e) {

		ReadFuture readFuture = this.getReadFuture();

		ReleaseUtil.release(readFuture);

		ReleaseUtil.release(this);

		IOEventHandle handle = readFuture.getIOEventHandle();

		if (handle == null) {
			logger.error(e.getMessage(), e);
			return;
		}

		try {
			handle.exceptionCaught(session, readFuture, e, IOEventState.WRITE);
		} catch (Throwable e1) {
			logger.debug(e1);
		}
	}

	public void onSuccess() {

		ReadFuture readFuture = this.getReadFuture();

		ReleaseUtil.release(readFuture);

		ReleaseUtil.release(this);

		IOEventHandle handle = readFuture.getIOEventHandle();

		if (handle == null) {
			return;
		}

		try {
			handle.futureSent(session, getReadFuture());
		} catch (Throwable e) {
			logger.debug(e);
		}
	}
	
	public boolean write() throws IOException {

		ByteBuf buffer = this.buffer;

		updateNetworkState(buffer.write(endPoint));

		return !buffer.hasRemaining();
	}

	public TCPEndPoint getEndPoint() {
		return endPoint;
	}

	public ReadFuture getReadFuture() {
		return readFuture;
	}

	public void setReadFuture(ReadFuture readFuture) {
		this.readFuture = readFuture;
	}

	public String toString() {
		return this.buffer.toString();
	}

	public void release() {
		ReleaseUtil.release(buffer);
	}
	
}
