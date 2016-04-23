package com.gifisan.nio.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.AbstractSession;
import com.gifisan.nio.component.ClientProtocolEncoder;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.ReadFuture;

public class DefaultClientSession extends AbstractSession implements ClientSession {

	private MessageBus			bus			= null;
	private ClientContext		context		= null;
	private ClientProtocolEncoder	encoder		= null;
	private EndPointWriter		endPointWriter	= null;
	private long				timeout		= 0;
	private AtomicBoolean		responsed		= new AtomicBoolean(true);

	public DefaultClientSession(EndPoint endPoint, byte sessionID) {
		super(endPoint, sessionID);
		this.bus = new MessageBus();
		this.context = (ClientContext) endPoint.getContext();
		this.encoder = context.getProtocolEncoder();
		this.endPointWriter = context.getEndPointWriter();
		this.serviceAcceptor = context.getServiceAcceptor();
	}

	public ReadFuture request(String serviceName, String content) throws IOException {
		return request(serviceName, content, null);
	}

	public ReadFuture request(String serviceName, String content, InputStream inputStream) throws IOException {

		if (responsed.compareAndSet(true, false)) {
			if (StringUtil.isNullOrBlank(serviceName)) {
				throw new IOException("empty service name");
			}

			byte[] array = content == null ? null : content.getBytes(context.getEncoding());

			IOWriteFuture future = encoder.encode(this, array, inputStream, context.getClientIOExceptionHandle());

			this.endPointWriter.offer(future);

			return this.poll(timeout);
		}

		throw new IOException("did not responsed");

	}

	public void offer(ReadFuture future) {
		this.bus.offer(future);
		this.offer();
	}
	
	public void offer() {
		responsed.set(true);
	}

	public ClientContext getContext() {
		return context;
	}

	public ReadFuture poll(long timeout) {
		return bus.poll(timeout);
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public void write(String serviceName, String content, OnReadFuture onReadFuture) throws IOException {
		write(serviceName, content, null, onReadFuture);
	}

	public void write(String serviceName, String content, InputStream inputStream, OnReadFuture onReadFuture)
			throws IOException {

		if (responsed.compareAndSet(true, false)) {
			if (StringUtil.isNullOrBlank(serviceName)) {
				throw new IOException("empty service name");
			}

			byte[] array = content == null ? null : content.getBytes(context.getEncoding());

			IOWriteFuture future = encoder.encode(this, array, inputStream, context.getClientIOExceptionHandle());

			this.endPointWriter.offer(future);

		}

		throw new IOException("did not responsed");
	}

}
