package com.gifisan.nio.server.service;

import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.Encoding;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.PropertiesLoader;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.DynamicClassLoader;
import com.gifisan.nio.component.PluginContext;
import com.gifisan.nio.server.ServerContext;

public class NormalPluginLoader extends AbstractLifeCycle implements PluginLoader {

	private ServerContext		context		= null;
	private DynamicClassLoader	classLoader	= null;
	private ReentrantLock		lock			= new ReentrantLock();
	private Logger				logger		= LoggerFactory.getLogger(NormalPluginLoader.class);
	private PluginContext[]		plugins		= new PluginContext[4];

	public NormalPluginLoader(ServerContext context, DynamicClassLoader classLoader) {
		this.context = context;
		this.classLoader = classLoader;
	}

	protected void doStart() throws Exception {

		loadPlugins(context, this.classLoader);

		this.initializePlugins(plugins);

	}

	protected void doStop() throws Exception {

		ReentrantLock lock = this.lock;

		lock.lock();
		
		for (PluginContext plugin : plugins) {
			
			if (plugin == null) {
				continue;
			}

			try {

				plugin.destroy(context, plugin.getConfig());

				logger.info("  [NIOServer] 卸载完成 [ {} ]", plugin);

			} catch (Throwable e) {

				logger.error(e.getMessage(), e);
			}
		}

		lock.unlock();
	}
	
	public PluginContext[] getPluginContexts() {
		return plugins;
	}

	private void initializePlugins(PluginContext[] plugins) throws Exception {
		
		for (PluginContext plugin : plugins) {
			
			if (plugin == null) {
				continue;
			}

			plugin.initialize(context, plugin.getConfig());

			logger.info("  [NIOServer] 加载完成 [ {} ]", plugin);
		}
	}

	private void loadPlugins(JSONArray array, DynamicClassLoader classLoader) throws Exception {

		if (array.size() > 4) {
			throw new IllegalArgumentException("plugin size max to 4");
		}
		
		for (int i = 0; i < array.size(); i++) {

			JSONObject object = array.getJSONObject(i);

			String className = object.getString("class");

			Configuration config = new Configuration(object);

			Class<?> clazz = classLoader.forName(className);

			PluginContext plugin = (PluginContext) clazz.newInstance();

			plugins[i] = plugin;

			plugin.setConfig(config);

		}
	}

	private void loadPlugins(ServerContext context, DynamicClassLoader classLoader) throws Exception {

		String config = PropertiesLoader.loadContent("plugins.config", Encoding.UTF8);

		if (!StringUtil.isNullOrBlank(config)) {

			JSONArray array = JSONObject.parseArray(config);

			loadPlugins(array, classLoader);

		}
	}

	public void prepare(ServerContext context, Configuration config) throws Exception {

		logger.info("  [NIOServer] 尝试加载新的Servlet配置......");

		loadPlugins(context, classLoader);

		logger.info("  [NIOServer] 尝试启动新的Servlet配置......");

		this.prepare(plugins);

	}

	private void prepare(PluginContext[] plugins) throws Exception {

		for (PluginContext plugin : plugins) {
			
			if (plugin == null) {
				continue;
			}

			plugin.prepare(context, plugin.getConfig());

			logger.info("  [NIOServer] 新的Servlet [ {} ] Prepare完成", plugin);

		}
	}

	public void unload(ServerContext context, Configuration config) throws Exception {

		ReentrantLock lock = this.lock;

		lock.lock();

		for (PluginContext plugin : plugins) {
			
			if (plugin == null) {
				continue;
			}

			try {

				plugin.unload(context, plugin.getConfig());

				logger.info("  [NIOServer] 旧的Servlet [ {} ] Unload完成", plugin);

			} catch (Throwable e) {

				logger.info("  [NIOServer] 旧的Servlet [ {} ] Unload失败", plugin);

				logger.error(e.getMessage(), e);

			}

		}
		lock.unlock();
	}

}
