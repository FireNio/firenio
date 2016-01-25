package com.gifisan.mtp.server;

import java.nio.charset.Charset;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.Encoding;
import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.common.SharedBundle;
import com.gifisan.mtp.component.AttributesImpl;
import com.gifisan.mtp.component.FilterService;
import com.gifisan.mtp.concurrent.ExecutorThreadPool;

public class ServerContextImpl extends AbstractLifeCycle implements ServerContext {

	private AttributesImpl		attributesImpl		= new AttributesImpl();
	private Charset			encoding			= Encoding.DEFAULT;
	private MTPServer			server			= null;
	private ServerEndpointFactory	endpointFactory	= null;
	private FilterService		filterService		= null;
	private ExecutorThreadPool	servletLazyExecutor	= null;
	private String				appLocalAddres		= null;
	private Logger				logger			= LoggerFactory.getLogger(ServerContextImpl.class);

	public ServerContextImpl(MTPServer server) {
		this.server = server;
		this.endpointFactory = new ServerEndpointFactory();
	}

	public void clearAttributes() {
		attributesImpl.clearAttributes();
	}

	protected void doStart() throws Exception {
		SharedBundle bundle = SharedBundle.instance();

		boolean debug = bundle.getBooleanProperty("SERVER.DEBUG");

		int CORE_SIZE = bundle.getIntegerProperty("SERVER.CORE_SIZE", 4);

		this.appLocalAddres = debug ? bundle.getBaseDIR() : bundle.getBaseDIR() + "app/";
		this.filterService = new FilterService(this);
		this.servletLazyExecutor = new ExecutorThreadPool(CORE_SIZE, "Servlet-accept-Job");

		logger.info("[MTPServer] 工作目录：" + appLocalAddres);

		this.filterService.start();
		this.servletLazyExecutor.start();
		this.endpointFactory.start();
	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(endpointFactory);
		LifeCycleUtil.stop(servletLazyExecutor);
		LifeCycleUtil.stop(filterService);
	}

	public Object getAttribute(String key) {
		return attributesImpl.getAttribute(key);
	}

	public Set<String> getAttributeNames() {
		return attributesImpl.getAttributeNames();
	}

	public Charset getEncoding() {
		return encoding;
	}

	public ServerEndpointFactory getServerEndpointFactory() {
		return endpointFactory;
	}

	public MTPServer getServer() {
		return server;
	}

	public String getAppLocalAddress() {
		return appLocalAddres;
	}

	public void removeAttribute(String key) {
		attributesImpl.removeAttribute(key);

	}

	public void setAttribute(String key, Object value) {
		attributesImpl.setAttribute(key, value);

	}

	public void setEncoding(Charset encoding) {
		this.encoding = encoding;
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

}
