package com.gifisan.nio.component.future;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.IOEventHandle;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.TCPEndPoint;

public abstract class AbstractWriteFuture extends FutureImpl implements IOWriteFuture {

	private IOEventHandle		handle		= null;
	private Session			session		= null;
	private byte[]			textCache		= null;
	private long				futureID		= 0;
	protected TCPEndPoint		endPoint		= null;
	protected ByteBuffer		textBuffer	= null;
	protected InputStream		inputStream	= null;
	private static AtomicLong	_autoFutureID	= new AtomicLong(0);
	private static final Logger logger = LoggerFactory.getLogger(AbstractWriteFuture.class);

	public AbstractWriteFuture(TCPEndPoint endPoint,IOEventHandle handle, String serviceName, ByteBuffer textBuffer, byte[] textCache,
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

	public void onException(IOException e) {
		
//		logger.error(e.getMessage(),e);
		
		if (this.handle == null) {
			return;
		}
		try {
			this.handle.handle(session, this, e);
		} catch (Throwable e1) {
			logger.debug(e1);
		}
	}
	
	public void onSuccess() {
		
//		logger.debug(">>>>>>>>>>>>>>>>>>>>> writed..");
		
		if (this.handle == null) {
			return;
		}
		try {
			this.handle.handle(session, this);
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
			}else{
				text = new String(textCache, session.getContext().getEncoding());
			}
			
			
		}
		return text;
	}

	public long getFutureID() {
		return futureID;
	}
}
