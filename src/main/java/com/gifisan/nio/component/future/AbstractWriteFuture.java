package com.gifisan.nio.component.future;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.IOExceptionHandle;
import com.gifisan.nio.component.IOWriteFuture;
import com.gifisan.nio.component.Session;

public abstract class AbstractWriteFuture extends FutureImpl implements IOWriteFuture {

	protected EndPoint			endPoint		= null;
	protected ByteBuffer		textBuffer	= null;
	protected InputStream		inputStream	= null;
	private IOExceptionHandle	handle		= null;
	private Session			session		= null;
	private byte[]			textCache		= null;
	private long				futureID;
	private static AtomicLong	_autoFutureID	= new AtomicLong(0);

	public AbstractWriteFuture(EndPoint endPoint,IOExceptionHandle handle, String serviceName, ByteBuffer textBuffer, byte[] textCache,
			Session session) {
		this.handle = handle;
		this.endPoint = endPoint;
		this.session = session;
		this.textBuffer = textBuffer;
		this.textCache = textCache;
		this.serviceName = serviceName;
		this.futureID = _autoFutureID.incrementAndGet();
	}

	protected void attackNetwork(int length) {

		endPoint.attackNetwork(length);
	}

	static AtomicInteger size = new AtomicInteger();
	
	public void catchException(IOException e) {
//		TestConcurrentCallBack.latch.countDown();
//		DebugUtil.debug("************============================"+e+TestConcurrentCallBack.latch.getCount());
		if (this.handle == null) {
			return;
		}
		try {
			this.handle.handle(session, this, e);
		} catch (Exception e1) {
			DebugUtil.debug(e1);
		}
	}

	public EndPoint getEndPoint() {
		return endPoint;
	}

	public boolean isNetworkWeak() {
		return endPoint.isNetworkWeak();
	}

	public Session getSession() {
		return session;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public String getText() {
		if (text == null) {
			text = new String(textCache, session.getContext().getEncoding());
		}
		return text;
	}

	public long getFutureID() {
		return futureID;
	}
}
