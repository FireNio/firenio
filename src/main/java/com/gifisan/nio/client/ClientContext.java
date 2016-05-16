package com.gifisan.nio.client;

import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.concurrent.ExecutorThreadPool;
import com.gifisan.nio.concurrent.ThreadPool;
import com.gifisan.nio.server.AbstractNIOContext;
import com.gifisan.nio.server.NIOContext;

public class ClientContext extends AbstractNIOContext implements NIOContext {

	private ClientIOExceptionHandle	clientIOExceptionHandle	= null;
	private String					serverHost			= null;
	private int					serverPort			= 0;
	private ThreadPool				executorThreadPool		= null;

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

	private ClientStreamAcceptor	clientStreamAcceptor	= null;

	public ClientStreamAcceptor getClientStreamAcceptor() {
		return clientStreamAcceptor;
	}

	protected void doStart() throws Exception {
		this.datagramPacketAcceptor = new RTPClientDPAcceptor();
		this.readFutureAcceptor = new ClientReadFutureAcceptor();
		this.outputStreamAcceptor = new ClientOutputStreamAcceptor();
		this.clientIOExceptionHandle = new ClientIOExceptionHandle();
		this.clientStreamAcceptor = new DefaultClientStreamAcceptor();
		this.executorThreadPool = new ExecutorThreadPool("JobExecutor", 1, 4);
		this.executorThreadPool.start();
	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(executorThreadPool);
	}

	public ThreadPool getExecutorThreadPool() {
		return executorThreadPool;
	}
	
	protected void setUDPEndPointFactory(ClientUDPEndPointFactory factory){
		this.udpEndPointFactory = factory;
	}

}
