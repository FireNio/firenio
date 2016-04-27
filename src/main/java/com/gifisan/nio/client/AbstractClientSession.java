package com.gifisan.nio.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.gifisan.nio.component.AbstractSession;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.future.ReadFuture;

public abstract class AbstractClientSession extends AbstractSession implements ProtectedClientSession {

	private Map<String, ClientStreamAcceptor>	streamAcceptors	= new HashMap<String, ClientStreamAcceptor>();
	protected MessageBus					messageBus		= null;
	protected ClientContext					context			= null;
	protected long						timeout			= 0;

	public AbstractClientSession(EndPoint endPoint, byte sessionID) {
		super(endPoint, sessionID);
		this.messageBus = new MessageBus();
		this.context = (ClientContext) endPoint.getContext();
	}

	public ReadFuture request(String serviceName, String content) throws IOException {
		return request(serviceName, content, null);
	}

	public void offer(ReadFuture future) {
		this.messageBus.offer(future);
	}

	public ClientContext getContext() {
		return context;
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

	public void onStreamRead(String key, ClientStreamAcceptor acceptor) {
		streamAcceptors.put(key, acceptor);
	}

	public ClientStreamAcceptor getStreamAcceptor(String serviceName) {
		return streamAcceptors.get(serviceName);
	}

}
