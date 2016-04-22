package com.gifisan.nio.client;

import com.gifisan.nio.component.ClientProtocolEncoder;
import com.gifisan.nio.component.DefaultProtocolDecoder;
import com.gifisan.nio.component.protocol.MultiDecoder;
import com.gifisan.nio.component.protocol.TextDecoder;
import com.gifisan.nio.server.AbstractNIOContext;
import com.gifisan.nio.server.NIOContext;

public class ClientContext extends AbstractNIOContext implements NIOContext{

	private ClientProtocolEncoder protocolEncoder = new ClientProtocolEncoder();
	
	private DefaultProtocolDecoder protocolDecoder = null;
	
	protected void doStart() throws Exception {
		this.readFutureAcceptor = new ClientReadFutureAcceptor();
		this.protocolDecoder = new DefaultProtocolDecoder(
				new TextDecoder(encoding), 
				new MultiDecoder(encoding));
		
		
	}

	protected void doStop() throws Exception {
		
		
	}

	public ClientProtocolEncoder getProtocolEncoder() {
		return protocolEncoder;
	}

	public DefaultProtocolDecoder getProtocolDecoder() {
		return protocolDecoder;
	}

	

	
	
	
}
