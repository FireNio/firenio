package com.gifisan.nio.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.SharedBundle;
import com.gifisan.nio.concurrent.ExecutorThreadPool;
import com.gifisan.nio.service.FilterService;

public class ServerContextImpl extends AbstractNIOContext implements ServerContext {

	private NIOServer			server			= null;
	private FilterService		filterService		= null;
	private ExecutorThreadPool	servletLazyExecutor	= null;
	private String				appLocalAddres		= null;
	private Logger				logger			= LoggerFactory.getLogger(ServerContextImpl.class);
	private int				serverPort		= 0;
	private int				serverCoreSize		= 4;

	protected void doStart() throws Exception {
		SharedBundle bundle = SharedBundle.instance();

		this.appLocalAddres = bundle.getBaseDIR() + "app/";
		this.filterService = new FilterService(this);
		this.servletLazyExecutor = new ExecutorThreadPool("Servlet-lazy-acceptor",serverCoreSize);
		this.selectionAcceptor = new  NIOSelectionAcceptor(this);
		

		logger.info("  [NIOServer] 工作目录：  { {} }", appLocalAddres);
		logger.info("  [NIOServer] 项目编码：  { {} }", getEncoding());
		logger.info("  [NIOServer] 监听端口：  { {} }", serverPort);
		logger.info("  [NIOServer] 服务器核数：{ {} }", serverCoreSize);

		
		this.filterService.start();
		this.endPointWriter.start();
		this.servletLazyExecutor.start();
	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(servletLazyExecutor);
		LifeCycleUtil.stop(filterService);
		LifeCycleUtil.stop(endPointWriter);
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

	public ExecutorThreadPool getExecutorThreadPool() {
		return servletLazyExecutor;
	}

	public boolean redeploy() {
		return this.filterService.redeploy();
	}

	private Map<String, Object>	attributes	= new HashMap<String, Object>();

	public Object removeAttribute(String key) {
		return this.attributes.remove(key);

	}

	public void setAttribute(String key, Object value) {
		this.attributes.put(key, value);
	}

	public Object getAttribute(String key) {
		return this.attributes.get(key);
	}

	public Set<String> getAttributeNames() {
		return this.attributes.keySet();
	}

	public void clearAttributes() {
		this.attributes.clear();
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

	
}
