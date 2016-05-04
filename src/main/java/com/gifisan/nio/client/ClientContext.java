package com.gifisan.nio.client;

import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.component.protocol.DefaultProtocolDecoder;
import com.gifisan.nio.server.AbstractNIOContext;
import com.gifisan.nio.server.NIOContext;

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
	
	private ClientStreamAcceptor clientStreamAcceptor = null;
	
	public ClientStreamAcceptor getClientStreamAcceptor() {
		return clientStreamAcceptor;
	}

	protected void doStart() throws Exception {
		this.readFutureAcceptor = new ClientReadFutureAcceptor();
		this.outputStreamAcceptor = new ClientOutputStreamAcceptor();
		this.clientIOExceptionHandle = new ClientIOExceptionHandle();
		this.clientStreamAcceptor = new DefaultClientStreamAcceptor();
		this.sessionFactory = new ClientSessionFactory();
		this.protocolDecoder = new DefaultProtocolDecoder();
		this.endPointWriter.start();
	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(endPointWriter);
	}

	public ClientProtocolEncoder getProtocolEncoder() {
		return protocolEncoder;
	}

	public DefaultProtocolDecoder getProtocolDecoder() {
		return protocolDecoder;
	}

}
