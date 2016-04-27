package com.gifisan.nio.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.IOWriteFuture;
import com.gifisan.nio.component.future.ReadFuture;

public class DefaultClientSession extends AbstractClientSession implements ProtectedClientSession {

	private AtomicBoolean				responsed		= new AtomicBoolean(true);

	public DefaultClientSession(EndPoint endPoint, byte sessionID) {
		super(endPoint, sessionID);
	}

	public ReadFuture request(String serviceName, String content, InputStream inputStream) throws IOException {

		if (responsed.compareAndSet(true, false)) {
			if (StringUtil.isNullOrBlank(serviceName)) {
				throw new IOException("empty service name");
			}

			byte[] array = content == null ? null : content.getBytes(context.getEncoding());

			IOWriteFuture future = encoder.encode(endPoint,this,serviceName,array, inputStream, context.getClientIOExceptionHandle());

			this.endPointWriter.offer(future);

			return this.poll(timeout);
		}

		throw new IOException("did not responsed");

	}

	public void offer() {
		responsed.set(true);
	}

	public ReadFuture poll(long timeout) {
		ReadFuture future = messageBus.poll(timeout);
		
		if (future == null) {
			return null;
		}
		
		this.messageBus.reset();
		
		this.offer();
		
		return future;
	}

	public void write(String serviceName, String content, InputStream inputStream, OnReadFuture onReadFuture)
			throws IOException {

		if (responsed.compareAndSet(true, false)) {
			if (StringUtil.isNullOrBlank(serviceName)) {
				throw new IOException("empty service name");
			}

			byte[] array = content == null ? null : content.getBytes(context.getEncoding());

			IOWriteFuture future = encoder.encode(endPoint,this,serviceName, array, inputStream, context.getClientIOExceptionHandle());

			if (onReadFuture == null) {
				onReadFuture = OnReadFuture.EMPTY_ON_READ_FUTURE;
			}
			
			this.messageBus.onReadFuture(onReadFuture);
			
			this.endPointWriter.offer(future);

		}

		throw new IOException("did not responsed");
	}
}
