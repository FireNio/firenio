package com.gifisan.nio.client;

import com.gifisan.nio.component.ClientProtocolEncoder;
import com.gifisan.nio.component.ClientServiceAcceptor;
import com.gifisan.nio.component.DefaultProtocolDecoder;
import com.gifisan.nio.component.protocol.MultiDecoder;
import com.gifisan.nio.component.protocol.TextDecoder;
import com.gifisan.nio.server.AbstractNIOContext;
import com.gifisan.nio.server.NIOContext;
import com.gifisan.nio.service.ServiceAcceptor;

public class ClientContext extends AbstractNIOContext implements NIOContext {

	private ClientProtocolEncoder		protocolEncoder		= new ClientProtocolEncoder();

	private DefaultProtocolDecoder	protocolDecoder		= null;

	private ClientIOExceptionHandle	clientIOExceptionHandle	= null;

	private String					serverHost			= null;

	private int					serverPort			= 0;
	
	public ClientContext(String serverHost, int serverPort) {
		this.serverHost = serverHost;
		this.serverPort = serverPort;
	}

	public String getServerHost() {
		return serverHost;
	}

	public int getServerPort() {
		return serverPort;
	}

	public ClientIOExceptionHandle getClientIOExceptionHandle() {
		return clientIOExceptionHandle;
	}

	private ServiceAcceptor	serviceAcceptor	= null;

	public ServiceAcceptor getServiceAcceptor() {
		return serviceAcceptor;
	}

	protected void doStart() throws Exception {
		this.readFutureAcceptor = new ClientReadFutureAcceptor();
		this.clientIOExceptionHandle = new ClientIOExceptionHandle();
		this.serviceAcceptor = new ClientServiceAcceptor();
		this.protocolDecoder = new DefaultProtocolDecoder(new TextDecoder(encoding), new MultiDecoder(encoding));
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
