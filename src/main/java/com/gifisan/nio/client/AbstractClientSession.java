package com.gifisan.nio.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.gifisan.nio.DisconnectException;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.component.AbstractSession;
import com.gifisan.nio.component.DatagramPacketAcceptor;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.server.service.impl.PutSession2FactoryServlet;

public abstract class AbstractClientSession extends AbstractSession implements ProtectedClientSession {

	private Map<String, ClientStreamAcceptor>	streamAcceptors	= new HashMap<String, ClientStreamAcceptor>();
	protected MessageBus					messageBus		= null;
	protected ClientContext					context			= null;
	protected long						timeout			= 0;
	protected DatagramPacketAcceptor			datagramPacketAcceptor = null;

	public AbstractClientSession(TCPEndPoint endPoint, byte logicSessionID) {
		super(endPoint, logicSessionID);
		this.context = (ClientContext) endPoint.getContext();
		this.messageBus = new MessageBus(this);	
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

	public void write(String serviceName, String content) throws IOException {
		write(serviceName, content, null);
	}

	public void onStreamRead(String key, ClientStreamAcceptor acceptor) {
		streamAcceptors.put(key, acceptor);
	}

	public ClientStreamAcceptor getStreamAcceptor(String serviceName) {
		return streamAcceptors.get(serviceName);
	}

	public void listen(String serviceName,OnReadFuture onReadFuture) throws IOException {
		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IOException("empty service name");
		}
		
		if (onReadFuture == null) {
			onReadFuture = OnReadFuture.EMPTY_ON_READ_FUTURE;
		}
		
		if (closed()) {
			throw DisconnectException.INSTANCE;
		}
		
		this.messageBus.listen(serviceName, onReadFuture);
		
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

	public String getSessionID() {
		if (sessionID == null) {
			
			try {
				ReadFuture future = request(PutSession2FactoryServlet.class.getSimpleName(), null);
				
				if (future instanceof ErrorReadFuture) {
					
					ErrorReadFuture _Future = ((ErrorReadFuture)future);
					
					throw new IOException(_Future.getException());
				}
				
				this.sessionID = future.getText();
			} catch (IOException e) {
				DebugUtil.debug(e);
			}
			
		}
		return sessionID;
	}

	public DatagramPacketAcceptor getDatagramPacketAcceptor() {
		return datagramPacketAcceptor;
	}

	public void setDatagramPacketAcceptor(DatagramPacketAcceptor datagramPacketAcceptor) {
		this.datagramPacketAcceptor = datagramPacketAcceptor;
	}
	
	

}
