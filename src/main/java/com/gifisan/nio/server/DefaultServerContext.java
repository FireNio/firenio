package com.gifisan.nio.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gifisan.nio.common.InitializeUtil;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.SharedBundle;
import com.gifisan.nio.component.DatagramPacketAcceptor;
import com.gifisan.nio.component.DynamicClassLoader;
import com.gifisan.nio.component.LoginCenter;
import com.gifisan.nio.component.PluginContext;
import com.gifisan.nio.component.ServerOutputStreamAcceptor;
import com.gifisan.nio.concurrent.ExecutorThreadPool;
import com.gifisan.nio.concurrent.ThreadPool;
import com.gifisan.nio.plugin.rtp.server.RTPServerDPAcceptor;
import com.gifisan.nio.server.service.FilterService;
import com.gifisan.nio.server.service.GenericServlet;
import com.gifisan.nio.server.service.NIOFilter;
import com.gifisan.nio.server.service.NormalPluginLoader;
import com.gifisan.nio.server.service.PluginLoader;

public class DefaultServerContext extends AbstractNIOContext implements ServerContext {

	private NIOServer					server			= null;
	private FilterService				filterService		= null;
	private String						appLocalAddres		= null;
	private int						serverPort		= 0;
	private int						serverCoreSize		= 4;
	private ThreadPool					serviceDispatcher	= null;
	private LoginCenter					loginCenter		= null;
	private PluginContext[]				pluginContexts		= new PluginContext[4];
	private Logger						logger			= LoggerFactory.getLogger(DefaultServerContext.class);
	private Map<String, GenericServlet>	pluginServlets		= new HashMap<String, GenericServlet>();
	private List<NIOFilter>				pluginFilters		= new ArrayList<NIOFilter>();
	private PluginLoader 				pluginLoader		= null;

	public DefaultServerContext(NIOServer server) {
		this.server = server;
	}

	protected void doStart() throws Exception {
		SharedBundle bundle = SharedBundle.instance();
		
		DynamicClassLoader classLoader = new DynamicClassLoader();

		this.appLocalAddres = bundle.getBaseDIR() + "app/";
		this.datagramPacketAcceptor = new RTPServerDPAcceptor();
		this.serviceDispatcher = new ExecutorThreadPool("Service-Executor", this.serverCoreSize);
		this.readFutureAcceptor = new ServerReadFutureAcceptor(serviceDispatcher);
		this.sessionFactory = new ServerSessionFactory();
		this.protocolDecoder = new ServerProtocolDecoder();
		this.loginCenter = new ServerLoginCenter();
		this.filterService = new FilterService(this,classLoader);
		this.outputStreamAcceptor = new ServerOutputStreamAcceptor(this);
		this.pluginLoader = new NormalPluginLoader(this,classLoader);
		
		logger.info("[NIOServer] ======================================= 服务开始启动 =======================================");
		logger.info("[NIOServer] 工作目录：  { {} }", appLocalAddres);
		logger.info("[NIOServer] 项目编码：  { {} }", encoding);
		logger.info("[NIOServer] 监听端口：  { {} }", serverPort);
		logger.info("[NIOServer] 服务器核数：{ {} }", serverCoreSize);
		
		this.pluginLoader.start();
		this.pluginContexts = pluginLoader.getPluginContexts();
		this.configPluginFilterAndServlet();

		this.filterService.start();
		this.loginCenter.initialize(this, null);
		this.serviceDispatcher.start();

	}
	
	private void configPluginFilterAndServlet(){
		
		for (PluginContext context : pluginContexts) {

			if (context == null) {
				continue;
			}

			context.configFilter(pluginFilters);
			context.configServlet(pluginServlets);
		}
	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(filterService);
		LifeCycleUtil.stop(serviceDispatcher);
		LifeCycleUtil.stop(pluginLoader);
		InitializeUtil.destroy(loginCenter, this, null);
		
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
		
		//FIXME plugin redeploy
		
		
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

	public LoginCenter getLoginCenter() {
		return loginCenter;
	}

	public PluginContext getPluginContext(Class clazz) {

		for (PluginContext context : pluginContexts) {

			if (context == null) {
				continue;
			}

			if (context.getClass().isAssignableFrom(clazz)) {
				return context;
			}
		}
		return null;
	}

	public Map<String, GenericServlet> getPluginServlets() {
		return pluginServlets;
	}

	public List<NIOFilter> getPluginFilters() {
		return pluginFilters;
	}
	
	public void setDatagramPacketAcceptor(DatagramPacketAcceptor datagramPacketAcceptor) {
		this.datagramPacketAcceptor = datagramPacketAcceptor;
	}
}
