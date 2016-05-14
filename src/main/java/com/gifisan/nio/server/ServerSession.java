package com.gifisan.nio.server;

import java.io.IOException;

import com.gifisan.nio.DisconnectException;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.component.AbstractSession;
import com.gifisan.nio.component.ActiveAuthority;
import com.gifisan.nio.component.Authority;
import com.gifisan.nio.component.IOEventHandle;
import com.gifisan.nio.component.IOWriteFuture;
import com.gifisan.nio.component.LoginCenter;
import com.gifisan.nio.component.ManagedIOSessionFactory;
import com.gifisan.nio.component.PluginContext;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.UDPEndPoint;
import com.gifisan.nio.component.future.IOReadFuture;
import com.gifisan.nio.component.future.ReadFuture;

public class ServerSession extends AbstractSession implements IOSession {

	private ServerContext		context		= null;
	private LoginCenter			loginCenter	= null;
	private UDPEndPoint			udpEndPoint	= null;
	private ActiveAuthority		authority		= null;
	private ActiveAuthority[]	authoritys	= new ActiveAuthority[4];

	public ServerSession(TCPEndPoint endPoint, byte logicSessionID) {
		super(endPoint, logicSessionID);

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
			DebugUtil.debug(e);
			IOEventHandle handle = _Future.getInputIOHandle();
			if (handle != null) {
				handle.handle(this, _Future, DisconnectException.INSTANCE);
			}
		}
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

	public void disconnect() {
		this.endPoint.endConnect();
		this.endPoint.getEndPointWriter().offer(new EmptyReadFuture(endPoint, this));
	}

	public void destroyImmediately() {

		ManagedIOSessionFactory factory = context.getManagedIOSessionFactory();

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

	public Authority getAuthority(PluginContext context) {

		if (context == null) {
			throw new IllegalArgumentException("null context");
		}

		return authoritys[context.getPluginIndex()];
	}
	
	public void setAuthority(PluginContext context,ActiveAuthority authority){
		
		if (context == null) {
			throw new IllegalArgumentException("null context");
		}
		
		authoritys[context.getPluginIndex()] = authority;
	}

	public UDPEndPoint getUDPEndPoint() {
		return udpEndPoint;
	}

}
