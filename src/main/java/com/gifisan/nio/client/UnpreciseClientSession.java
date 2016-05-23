package com.gifisan.nio.client;

import java.io.IOException;
import java.io.InputStream;

import com.gifisan.nio.DisconnectException;
import com.gifisan.nio.TimeoutException;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.IOWriteFuture;
import com.gifisan.nio.component.future.ReadFuture;

public class UnpreciseClientSession extends AbstractClientSession {

	public UnpreciseClientSession(TCPEndPoint endPoint) {
		super(endPoint);
	}

	public ReadFuture request(String serviceName, String content, InputStream inputStream,long timeout) throws IOException {

		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IOException("empty service name");
		}

		byte[] array = content == null ? null : content.getBytes(context.getEncoding());

		IOWriteFuture future = encoder.encode(endPoint, this, serviceName, array, inputStream,
				context.getClientIOExceptionHandle());

		if (closed()) {
			throw DisconnectException.INSTANCE;
		}
		
		WaiterOnReadFuture onReadFuture = new WaiterOnReadFuture();
		
		listen(serviceName, onReadFuture);

		this.endPointWriter.offer(future);

		if (onReadFuture.await(timeout)) {
			
			return onReadFuture.getReadFuture();
		}
		
		throw new TimeoutException("timeout");
	}

	public void write(String serviceName, String content, InputStream inputStream) throws IOException {

		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IOException("empty service name");
		}

		byte[] array = content == null ? null : content.getBytes(context.getEncoding());

		IOWriteFuture future = encoder.encode(endPoint, this, serviceName, array, inputStream,
				context.getClientIOExceptionHandle());

		if (closed()) {
			throw DisconnectException.INSTANCE;
		}

		this.endPointWriter.offer(future);
	}

}
