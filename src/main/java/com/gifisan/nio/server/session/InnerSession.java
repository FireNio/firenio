package com.gifisan.nio.server.session;

import com.gifisan.nio.component.ProtocolData;
import com.gifisan.nio.server.InnerRequest;
import com.gifisan.nio.server.InnerResponse;
import com.gifisan.nio.server.selector.ServiceAcceptorJob;

public interface InnerSession extends Session {
	
	public abstract byte getSessionID();
	
	public abstract void destroyImmediately();
	
	public abstract ServiceAcceptorJob updateAcceptor(ProtocolData decoder);
	
	public abstract ServiceAcceptorJob updateAcceptor();
	
	public abstract InnerRequest getRequest();
	
	public abstract InnerResponse getResponse();
	
}
