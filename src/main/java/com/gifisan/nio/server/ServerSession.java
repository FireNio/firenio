package com.gifisan.nio.server;

import java.io.IOException;

import com.gifisan.nio.DisconnectException;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.UUIDGenerator;
import com.gifisan.nio.component.AbstractSession;
import com.gifisan.nio.component.IOEventHandle;
import com.gifisan.nio.component.IOWriteFuture;
import com.gifisan.nio.component.LoginCenter;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.UDPEndPoint;
import com.gifisan.nio.component.future.IOReadFuture;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.security.Authority;
import com.gifisan.security.AuthorityManager;

public class ServerSession extends AbstractSession implements IOSession {

	private ServerContext		context			= null;
	private LoginCenter			loginCenter		= null;
	private UDPEndPoint			udpEndPoint		= null;
	private AuthorityManager		authorityManager	= null;
	private static final Logger	logger			= LoggerFactory.getLogger(ServerSession.class);
	

	public ServerSession(TCPEndPoint endPoint) {
		super(endPoint);

		this.context = (ServerContext) endPoint.getContext();
		this.loginCenter = context.getLoginCenter();
	}

	public void flush(ReadFuture future) {
		IOReadFuture _Future = (IOReadFuture) future;

		if (_Future.flushed()) {
			throw new IllegalStateException("flushed already");
		}

		if (!endPoint.isOpened()) {
			IOEventHandle handle = _Future.getInputIOHandle();
			if (handle != null) {
				handle.handle(this, _Future, DisconnectException.INSTANCE);
			}
			return;
		}

		try {
			IOWriteFuture writeFuture = encoder.encode(endPoint, this, _Future.getServiceName(), _Future
					.getTextCache().toByteArray(), _Future.getInputStream(), _Future.getInputIOHandle());

			_Future.flush();

			writeFuture.attach(_Future.attachment());

			this.endPointWriter.offer(writeFuture);
		} catch (IOException e) {
			logger.debug(e);
			IOEventHandle handle = _Future.getInputIOHandle();
			if (handle != null) {
				handle.handle(this, _Future, DisconnectException.INSTANCE);
			}
		}
	}


	public ServerContext getContext() {
		return context;
	}


	public LoginCenter getLoginCenter() {
		return loginCenter;
	}

	public void disconnect() {
		this.endPoint.endConnect();
		this.endPoint.getEndPointWriter().offer(new EmptyReadFuture(endPoint, this));
	}

	public void destroyImmediately() {

		SessionFactory factory = context.getSessionFactory();

		factory.removeIOSession(this);

		CloseUtil.close(udpEndPoint);

		super.destroyImmediately();
	}

	protected TCPEndPoint getEndPoint() {
		return super.getEndPoint();
	}

	public void setUDPEndPoint(UDPEndPoint udpEndPoint) {

		if (this.udpEndPoint != null && this.udpEndPoint != udpEndPoint) {
			throw new IllegalArgumentException("udpEndPoint setted");
		}

		this.udpEndPoint = udpEndPoint;
	}
	
	public AuthorityManager getAuthorityManager() {
		return authorityManager;
	}

	public void setAuthorityManager(AuthorityManager authorityManager) {
		this.authorityManager = authorityManager;
		if (authorityManager.getAuthority().getRoleID() == Authority.GUEST.getRoleID()) {
			return;
		}
		this.sessionID = UUIDGenerator.random();
		this.context.getSessionFactory().putIOSession(this);
	}

	public UDPEndPoint getUDPEndPoint() {
		return udpEndPoint;
	}

	public Authority getAuthority() {
		
		return authorityManager.getAuthority();
	}
}
