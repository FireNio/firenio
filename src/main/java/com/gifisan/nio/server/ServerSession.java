package com.gifisan.nio.server;

import java.io.IOException;

import com.gifisan.nio.DisconnectException;
import com.gifisan.nio.FlushedException;
import com.gifisan.nio.component.AbstractSession;
import com.gifisan.nio.component.ActiveAuthority;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.IOExceptionHandle;
import com.gifisan.nio.component.IOWriteFuture;
import com.gifisan.nio.component.LoginCenter;
import com.gifisan.nio.component.future.IOReadFuture;
import com.gifisan.nio.component.future.ReadFuture;

public class ServerSession extends AbstractSession implements IOSession {

	private ActiveAuthority	authority		= null;
	private ServerContext	context		= null;
	private LoginCenter		loginCenter	= null;

	public ServerSession(EndPoint endPoint, byte sessionID) {
		super(endPoint, sessionID);

		this.context = (ServerContext) endPoint.getContext();
		this.loginCenter = context.getLoginCenter();
	}

	public void flush(ReadFuture future) throws IOException {
		IOReadFuture _Future = (IOReadFuture) future;

		if (_Future.flushed()) {
			throw new FlushedException("flushed already");
		}

		if (!endPoint.isOpened()) {
			IOExceptionHandle handle = _Future.getInputIOHandle();
			if (handle != null) {
				handle.handle(this, _Future, DisconnectException.INSTANCE);
			}
			return;
		}

		IOWriteFuture writeFuture = encoder.encode(
				endPoint, 
				this, 
				_Future.getServiceName(), 
				_Future.getTextCache().toByteArray(), 
				_Future.getInputStream(), 
				_Future.getInputIOHandle());

		_Future.flush();

		this.endPointWriter.offer(writeFuture);
	}

	public ActiveAuthority getAuthority() {
		return authority;
	}

	public ServerContext getContext() {
		return context;
	}

	public void setAuthority(ActiveAuthority authority) {
		this.authority = authority;
	}

	public LoginCenter getLoginCenter() {
		return loginCenter;
	}

}
