package com.gifisan.nio.server;

import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.SharedBundle;
import com.gifisan.nio.component.ServerProtocolDecoder;
import com.gifisan.nio.component.ServerProtocolEncoder;
import com.gifisan.nio.component.ServerReadFutureAcceptor;
import com.gifisan.nio.component.protocol.MultiDecoder;
import com.gifisan.nio.component.protocol.TextDecoder;
import com.gifisan.nio.concurrent.ExecutorThreadPool;
import com.gifisan.nio.concurrent.ThreadPool;
import com.gifisan.nio.service.FilterService;

public class ServerContextImpl extends AbstractNIOContext implements ServerContext {

	private NIOServer				server			= null;
	private FilterService			filterService		= null;
	private String					appLocalAddres		= null;
	private Logger					logger			= LoggerFactory.getLogger(ServerContextImpl.class);
	private int					serverPort		= 0;
	private int					serverCoreSize		= 4;
	private ThreadPool				serviceDispatcher	= null;
	private ServerProtocolDecoder		protocolDecoder	= null;
	private ServerProtocolEncoder		protocolEncoder	= null;

	public ServerContextImpl(NIOServer server) {
		this.server = server;
	}

	protected void doStart() throws Exception {
		SharedBundle bundle = SharedBundle.instance();

		this.appLocalAddres = bundle.getBaseDIR() + "app/";
		this.serviceDispatcher = new ExecutorThreadPool("Service-Executor", this.serverCoreSize);
		this.readFutureAcceptor = new ServerReadFutureAcceptor(serviceDispatcher);
		this.protocolDecoder = new ServerProtocolDecoder(
				new TextDecoder(encoding), 
				new MultiDecoder(encoding));
		this.protocolEncoder = new ServerProtocolEncoder();
		this.filterService = new FilterService(this);
		this.selectionAcceptor = new NIOSelectionAcceptor(this);

		logger.info("  [NIOServer] 工作目录：  { {} }", appLocalAddres);
		logger.info("  [NIOServer] 项目编码：  { {} }", encoding);
		logger.info("  [NIOServer] 监听端口：  { {} }", serverPort);
		logger.info("  [NIOServer] 服务器核数：{ {} }", serverCoreSize);

		this.serviceDispatcher.start();
		this.filterService.start();

		super.doStart();
	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(filterService);
		LifeCycleUtil.stop(serviceDispatcher);
		super.doStop();
	}

	public NIOServer getServer() {
		return server;
	}

	public String getAppLocalAddress() {
		return appLocalAddres;
	}

	public FilterService getFilterService() {
		return filterService;
	}

	public boolean redeploy() {
		return this.filterService.redeploy();
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public int getServerCoreSize() {
		return serverCoreSize;
	}

	public void setServerCoreSize(int serverCoreSize) {
		this.serverCoreSize = serverCoreSize;
	}

	public ServerProtocolDecoder getProtocolDecoder() {
		return protocolDecoder;
	}

	public ServerProtocolEncoder getProtocolEncoder() {
		return protocolEncoder;
	}

}
