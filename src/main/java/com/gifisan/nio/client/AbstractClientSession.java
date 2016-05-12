package com.gifisan.nio.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.gifisan.nio.DisconnectException;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.component.AbstractSession;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.IOWriteFuture;
import com.gifisan.nio.component.future.ReadFuture;

public abstract class AbstractClientSession extends AbstractSession implements ProtectedClientSession {

	private Map<String, ClientStreamAcceptor>	streamAcceptors	= new HashMap<String, ClientStreamAcceptor>();
	protected MessageBus					messageBus		= null;
	protected ClientContext					context			= null;
	protected long						timeout			= 0;

	public AbstractClientSession(TCPEndPoint endPoint, byte logicSessionID) {
		super(endPoint, logicSessionID);
		this.messageBus = new MessageBus(this);
		this.context = (ClientContext) endPoint.getContext();
	}

	public ReadFuture request(String serviceName, String content) throws IOException {
		return request(serviceName, content, null);
	}

	public void offer(ReadFuture future) {
		this.messageBus.filterOffer(future);
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

	public void listen(String serviceName, String content, OnReadFuture onReadFuture) throws IOException {
		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IOException("empty service name");
		}
		
		byte[] array = content == null ? null : content.getBytes(context.getEncoding());

		IOWriteFuture future = encoder.encode(endPoint,this,serviceName, array, null, context.getClientIOExceptionHandle());

		if (onReadFuture == null) {
			onReadFuture = OnReadFuture.EMPTY_ON_READ_FUTURE;
		}
		
		if (closed()) {
			throw DisconnectException.INSTANCE;
		}
		
		this.messageBus.listen(serviceName, onReadFuture);
		
		this.endPointWriter.offer(future);
	}
	
	public void cancelListen(String serviceName){
		this.messageBus.cancelListen(serviceName);
	}

	public MessageBus getMessageBus() {
		return messageBus;
	}

	public void destroyImmediately() {
		
		MessageBus bus = this.messageBus;
		
		for(;bus.size() > 0;){
			
			ThreadUtil.sleep(8);
		}
		
		super.destroyImmediately();
	}

}
