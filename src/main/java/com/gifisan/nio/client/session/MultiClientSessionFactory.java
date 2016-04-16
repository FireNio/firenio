package com.gifisan.nio.client.session;

import java.util.concurrent.atomic.AtomicInteger;

import com.gifisan.nio.client.ClientRequestTask;
import com.gifisan.nio.client.ClientSesssion;
import com.gifisan.nio.client.MessageBus;

public class MultiClientSessionFactory implements ClientSessionFactory {
	
	private AtomicInteger		sessionIndex			= new AtomicInteger(-1);
	private MessageBus[]		buses				= null;
	private ClientSesssion[]		sessions				= new ClientSesssion[4];
	private ClientRequestTask	requestTask			= null;

	public MultiClientSessionFactory(MessageBus[] buses, ClientRequestTask requestTask) {
		this.buses = buses;
		this.requestTask = requestTask;
	}

	public ClientSesssion getClientSesssion() {
		int _sessionID = sessionIndex.incrementAndGet();
		if (_sessionID > 4) {
			return null;
		}
		byte sessionID = (byte) _sessionID;
		MessageBus bus = new MessageBus();
		ClientSesssion session = new MultiSession(requestTask, bus, sessionID);
		buses[sessionID] = bus;
		sessions[sessionID] = session;
		return session;
	}

	public int getSessionSize() {
		return 1;
	}

}
