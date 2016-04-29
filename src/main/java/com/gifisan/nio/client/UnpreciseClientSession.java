package com.gifisan.nio.client;

import java.io.IOException;
import java.io.InputStream;

import com.gifisan.nio.WriterOverflowException;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.IOWriteFuture;
import com.gifisan.nio.component.future.ReadFuture;

public class UnpreciseClientSession extends AbstractClientSession implements ProtectedClientSession {

	public UnpreciseClientSession(EndPoint endPoint, byte sessionID) {
		super(endPoint, sessionID);
	}

	public ReadFuture request(String serviceName, String content, InputStream inputStream) throws IOException {

		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IOException("empty service name");
		}

		byte[] array = content == null ? null : content.getBytes(context.getEncoding());

		IOWriteFuture future = encoder.encode(endPoint,this,serviceName,array, inputStream, context.getClientIOExceptionHandle());

		if (!this.endPointWriter.offer(future)) {
			throw WriterOverflowException.INSTANCE;
		}

		return this.poll(timeout);

	}

	public void offer() {
		
	}
	
	public ReadFuture poll(long timeout) {
		return messageBus.poll(timeout);
	}

	public void write(String serviceName, String content, InputStream inputStream, OnReadFuture onReadFuture)
			throws IOException {

		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IOException("empty service name");
		}

		byte[] array = content == null ? null : content.getBytes(context.getEncoding());

		IOWriteFuture future = encoder.encode(endPoint,this,serviceName, array, inputStream, context.getClientIOExceptionHandle());

		if (onReadFuture == null) {
			onReadFuture = OnReadFuture.EMPTY_ON_READ_FUTURE;
		}
		
		this.messageBus.onReadFuture(onReadFuture);
		
		if (!this.endPointWriter.offer(future)) {
			future.catchException(WriterOverflowException.INSTANCE);
		}
	}


}
