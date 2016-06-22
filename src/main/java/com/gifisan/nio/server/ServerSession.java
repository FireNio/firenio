package com.gifisan.nio.server;

import java.io.IOException;

import com.gifisan.nio.DisconnectException;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.AbstractSession;
import com.gifisan.nio.component.IOEventHandle;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.UDPEndPoint;
import com.gifisan.nio.component.future.IOReadFuture;
import com.gifisan.nio.component.future.IOWriteFuture;
import com.gifisan.nio.component.future.ReadFuture;

public class ServerSession extends AbstractSession implements Session {

	private NIOContext			context			= null;
	private UDPEndPoint			udpEndPoint		= null;
	private static final Logger	logger			= LoggerFactory.getLogger(ServerSession.class);

	public ServerSession(TCPEndPoint endPoint) {
		super(endPoint);

		this.context = (NIOContext) endPoint.getContext();
	}

	public void flush(ReadFuture future) {
		
		IOReadFuture _future = (IOReadFuture) future;

		if (_future.flushed()) {
			throw new IllegalStateException("flushed already");
		}

		IOEventHandle handle = context.getIOEventHandle();

		if (!endPoint.isOpened()) {
			
			handle.exceptionCaughtOnWrite(this, future, null, DisconnectException.INSTANCE);
			
			return;
		}
		
		IOWriteFuture writeFuture = null;

		try {
			
			writeFuture = encoder.encode(endPoint, 0, _future.getServiceName(), _future.getTextCache()
					.toByteArray(), _future.getInputStream());

			_future.flush();

			writeFuture.attach(_future.attachment());

			this.endPointWriter.offer(writeFuture);
		} catch (IOException e) {
			
			logger.debug(e.getMessage(),e);
			
			handle.exceptionCaughtOnWrite(this, future, writeFuture, e);
		}
	}

	public void disconnect() {
		this.endPoint.endConnect();
		this.endPoint.getEndPointWriter().offer(new EmptyReadFuture(endPoint));
	}

	public void destroy() {

		SessionFactory factory = context.getSessionFactory();

		factory.removeSession(this);

		CloseUtil.close(udpEndPoint);

		super.destroy();
	}

	public void setUDPEndPoint(UDPEndPoint udpEndPoint) {

		if (this.udpEndPoint != null && this.udpEndPoint != udpEndPoint) {
			throw new IllegalArgumentException("udpEndPoint setted");
		}

		this.udpEndPoint = udpEndPoint;
	}

	public UDPEndPoint getUDPEndPoint() {
		return udpEndPoint;
	}

}
