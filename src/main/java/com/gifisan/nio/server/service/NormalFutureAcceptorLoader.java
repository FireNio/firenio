package com.gifisan.nio.server.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.ApplicationContext;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.DynamicClassLoader;
import com.gifisan.nio.component.ReadFutureAcceptor;
import com.gifisan.nio.server.configuration.ServletsConfiguration;

public class NormalFutureAcceptorLoader extends AbstractLifeCycle implements FutureAcceptorServiceLoader {

	private ApplicationContext				context		= null;
	private DynamicClassLoader			classLoader	= null;
	private Logger						logger		= LoggerFactory.getLogger(NormalFutureAcceptorLoader.class);
	private Map<String, GenericReadFutureAcceptor>	servlets		= new LinkedHashMap<String, GenericReadFutureAcceptor>();
	private ServletsConfiguration			configuration	= null;
	
	public NormalFutureAcceptorLoader(ApplicationContext context, DynamicClassLoader classLoader) {
		this.configuration = context.getConfiguration().getServletsConfiguration();
		this.context = context;
		this.classLoader = classLoader;
	}

	protected void doStart() throws Exception {

		Map<String, GenericReadFutureAcceptor> servlets = loadServlets(configuration,classLoader);

		this.initializeServlets(servlets);

		this.servlets = servlets;

	}

	protected void doStop() throws Exception {

		Set<Entry<String, GenericReadFutureAcceptor>> entries = servlets.entrySet();

		for (Entry<String, GenericReadFutureAcceptor> entry : entries) {

			GenericReadFutureAcceptor servlet = entry.getValue();

			try {

				servlet.destroy(context, servlet.getConfig());

				logger.info(" [NIOServer] 卸载完成 [ {} ]", servlet);

			} catch (Throwable e) {

				logger.error(e.getMessage(), e);
			}
		}
	}


	public ReadFutureAcceptor getFutureAcceptor(String serviceName) {
		return servlets.get(serviceName);
	}

	private void initializeServlets(Map<String, GenericReadFutureAcceptor> servlets) throws Exception {

		Set<Entry<String, GenericReadFutureAcceptor>> entries = servlets.entrySet();

		for (Entry<String, GenericReadFutureAcceptor> entry : entries) {

			GenericReadFutureAcceptor servlet = entry.getValue();

			servlet.initialize(context, servlet.getConfig());

			logger.info(" [NIOServer] 加载完成 [ {} ]", servlet);

		}
	}

	private Map<String, GenericReadFutureAcceptor> loadServlets(ServletsConfiguration configuration,DynamicClassLoader classLoader) throws Exception {

		List<Configuration> servletConfigurations = configuration.getServlets();
		
		Map<String, GenericReadFutureAcceptor> pluginServlets = context.getPluginServlets();
		
		Map<String, GenericReadFutureAcceptor> servlets = new LinkedHashMap<String, GenericReadFutureAcceptor>();

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

			GenericReadFutureAcceptor servlet = (GenericReadFutureAcceptor) clazz.newInstance();

			servlets.put(serviceName, servlet);

			servlet.setConfig(config);

		}
		return servlets;
	}

	public void prepare(ApplicationContext context, Configuration config) throws Exception {

		logger.info(" [NIOServer] 尝试加载新的Servlet配置......");

		this.servlets = loadServlets(configuration, classLoader);

		logger.info(" [NIOServer] 尝试启动新的Servlet配置......");

		this.prepare(servlets);
		
		this.softStart();

	}

	private void prepare(Map<String, GenericReadFutureAcceptor> servlets) throws Exception {

		Set<Entry<String, GenericReadFutureAcceptor>> entries = servlets.entrySet();

		for (Entry<String, GenericReadFutureAcceptor> entry : entries) {

			GenericReadFutureAcceptor servlet = entry.getValue();

			servlet.prepare(context, servlet.getConfig());

			logger.info(" [NIOServer] 新的Servlet [ {} ] Prepare完成", servlet);

		}
	}

	public void unload(ApplicationContext context, Configuration config) throws Exception {

		Set<Entry<String, GenericReadFutureAcceptor>> entries = servlets.entrySet();

		for (Entry<String, GenericReadFutureAcceptor> entry : entries) {

			GenericReadFutureAcceptor servlet = entry.getValue();

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
