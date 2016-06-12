package com.gifisan.nio.server.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.DynamicClassLoader;
import com.gifisan.nio.server.DefaultServerContext;
import com.gifisan.nio.server.FilterAcceptor;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.server.configuration.ServletsConfiguration;

public class NormalServletLoader extends AbstractLifeCycle implements ServletLoader {

	private ServerContext				context		= null;
	private DynamicClassLoader			classLoader	= null;
	private Logger						logger		= LoggerFactory.getLogger(NormalServletLoader.class);
	private Map<String, GenericServlet>	servlets		= new LinkedHashMap<String, GenericServlet>();
	private ServletsConfiguration			configuration	= null;
	
	public NormalServletLoader(ServerContext context, DynamicClassLoader classLoader) {
		this.configuration = context.getConfiguration().getServletsConfiguration();
		this.context = context;
		this.classLoader = classLoader;
	}

	protected void doStart() throws Exception {

		Map<String, GenericServlet> servlets = loadServlets(configuration,classLoader);

		this.initializeServlets(servlets);

		this.servlets = servlets;

	}

	protected void doStop() throws Exception {

		Set<Entry<String, GenericServlet>> entries = servlets.entrySet();

		for (Entry<String, GenericServlet> entry : entries) {

			GenericServlet servlet = entry.getValue();

			try {

				servlet.destroy(context, servlet.getConfig());

				logger.info(" [NIOServer] 卸载完成 [ {} ]", servlet);

			} catch (Throwable e) {

				logger.error(e.getMessage(), e);
			}
		}
	}

	public FilterAcceptor getServlet(String serviceName) {

		return servlets.get(serviceName);

	}

	private void initializeServlets(Map<String, GenericServlet> servlets) throws Exception {

		Set<Entry<String, GenericServlet>> entries = servlets.entrySet();

		for (Entry<String, GenericServlet> entry : entries) {

			GenericServlet servlet = entry.getValue();

			servlet.initialize(context, servlet.getConfig());

			logger.info(" [NIOServer] 加载完成 [ {} ]", servlet);

		}
	}

	private Map<String, GenericServlet> loadServlets(ServletsConfiguration configuration,DynamicClassLoader classLoader) throws Exception {

		List<Configuration> servletConfigurations = configuration.getServlets();
		
		DefaultServerContext context2 = (DefaultServerContext)context;
		
		Map<String, GenericServlet> pluginServlets = context2.getPluginServlets();
		
		Map<String, GenericServlet> servlets = new LinkedHashMap<String, GenericServlet>();

		servlets.putAll(pluginServlets);

		if (servletConfigurations.size() == 0) {
			
			if (servlets.size() == 0) {
				
				throw new Error("empty servlet config");
			}
			return servlets;
		}

		for (int i = 0; i < servletConfigurations.size(); i++) {

			Configuration config = servletConfigurations.get(i);

			String className = config.getParameter("class","empty");

			Class<?> clazz = classLoader.forName(className);

			String serviceName = config.getParameter("serviceName",clazz.getSimpleName());

			if (servlets.containsKey(serviceName)) {
				throw new IllegalArgumentException("repeat servlet[ " + serviceName + "@" + className + " ]");
			}

			GenericServlet servlet = (GenericServlet) clazz.newInstance();

			servlets.put(serviceName, servlet);

			servlet.setConfig(config);

		}
		return servlets;
	}

	public void prepare(ServerContext context, Configuration config) throws Exception {

		logger.info(" [NIOServer] 尝试加载新的Servlet配置......");

		this.servlets = loadServlets(configuration, classLoader);

		logger.info(" [NIOServer] 尝试启动新的Servlet配置......");

		this.prepare(servlets);
		
		this.softStart();

	}

	private void prepare(Map<String, GenericServlet> servlets) throws Exception {

		Set<Entry<String, GenericServlet>> entries = servlets.entrySet();

		for (Entry<String, GenericServlet> entry : entries) {

			GenericServlet servlet = entry.getValue();

			servlet.prepare(context, servlet.getConfig());

			logger.info(" [NIOServer] 新的Servlet [ {} ] Prepare完成", servlet);

		}
	}

	public void unload(ServerContext context, Configuration config) throws Exception {

		Set<Entry<String, GenericServlet>> entries = servlets.entrySet();

		for (Entry<String, GenericServlet> entry : entries) {

			GenericServlet servlet = entry.getValue();

			try {

				servlet.unload(context, servlet.getConfig());

				logger.info(" [NIOServer] 旧的Servlet [ {} ] Unload完成", servlet);

			} catch (Throwable e) {

				logger.info(" [NIOServer] 旧的Servlet [ {} ] Unload失败", servlet);

				logger.error(e.getMessage(), e);

			}
		}
	}

}
