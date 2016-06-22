package com.gifisan.nio.component.future;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.IOEventHandle;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.TCPEndPoint;

public abstract class AbstractWriteFuture extends FutureImpl implements IOWriteFuture {

	private Session			session		= null;
	private byte[]			textCache		= null;
	protected TCPEndPoint		endPoint		= null;
	protected ByteBuffer		textBuffer	= null;
	protected InputStream		inputStream	= null;
	private static final Logger	logger		= LoggerFactory.getLogger(AbstractWriteFuture.class);

	public AbstractWriteFuture(TCPEndPoint endPoint, Integer futureID, String serviceName, ByteBuffer textBuffer,
			byte[] textCache) {
		this.endPoint = endPoint;
		this.session = endPoint.getSession();
		this.textBuffer = textBuffer;
		this.textCache = textCache;
		this.serviceName = serviceName;
		this.futureID = futureID;
	}

	protected void attackNetwork(int length) {

		endPoint.attackNetwork(length);
	}

	public void onException(IOException e) {

		// logger.error(e.getMessage(),e);

		IOEventHandle handle = endPoint.getContext().getIOEventHandle();
		try {
			// FIXME
			handle.exceptionCaughtOnWrite(session, null, this, e);
		} catch (Throwable e1) {
			logger.debug(e1);
		}
	}

	public void onSuccess() {

		// logger.debug(">>>>>>>>>>>>>>>>>>>>> writed..");

		IOEventHandle handle = endPoint.getContext().getIOEventHandle();

		try {
			handle.futureSent(session, this);
		} catch (Throwable e) {
			logger.debug(e);
		}
	}

	public TCPEndPoint getEndPoint() {
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
			if (textCache == null) {
				text = "";
			} else {
				text = new String(textCache, session.getContext().getEncoding());
			}

		}
		return text;
	}
}
