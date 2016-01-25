package com.gifisan.mtp.servlet;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.Encoding;
import com.gifisan.mtp.common.FileUtil;
import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.component.DynamicClassLoader;
import com.gifisan.mtp.component.ServletConfig;
import com.gifisan.mtp.server.ServerContext;
import com.gifisan.mtp.server.ServletAcceptor;
import com.gifisan.mtp.servlet.impl.ErrorServlet;

public class NormalServletLoader extends AbstractLifeCycle implements ServletLoader {

	private ServerContext				context		= null;
	private final Logger				logger		= LoggerFactory.getLogger(NormalServletLoader.class);
	private Map<String, GenericServlet>	servlets		= new LinkedHashMap<String, GenericServlet>();
	private DynamicClassLoader			classLoader	= null;

	public NormalServletLoader(ServerContext context, DynamicClassLoader classLoader) {
		this.context = context;
		this.classLoader = classLoader;
	}

	protected void doStart() throws Exception {
		Map<String, GenericServlet> servlets = loadServlets(context);

		this.initializeServlets(servlets);

		this.servlets = servlets;

	}

	protected void doStop() throws Exception {
		this.destroyServlets(this.servlets);
	}

	public ServletAcceptor getServlet(String serviceName) {
		return servlets.get(serviceName);
	}

	private void initializeServlets(Map<String, GenericServlet> servlets) {
		Set<Entry<String, GenericServlet>> entries = servlets.entrySet();
		for (Entry<String, GenericServlet> entry : entries) {
			GenericServlet servlet = entry.getValue();
			try {
				logger.info("[MTPServer] 加载完成："+servlet);
				servlet.initialize(context, servlet.getConfig());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				String serviceName = entry.getKey();
				ErrorServlet error = new ErrorServlet(e);
				servlets.put(serviceName, error);
			}
		}
	}

	private Map<String, GenericServlet> loadServlets(JSONArray array) throws Exception {
		Map<String, GenericServlet> servlets = new LinkedHashMap<String, GenericServlet>();
		if (array.size() == 0) {
			throw new Exception("empty servlet config");
		}
		for (int i = 0; i < array.size(); i++) {
			JSONObject object = array.getJSONObject(i);
			String className = object.getString("class");
			String serviceName = object.getString("serviceName");
			ServletConfig config = new ServletConfig();
			Map<String, Object> map = reflectMap(object);
			config.setConfig(map);
			try {
				Class<?> clazz = classLoader.forName(className);
				if (StringUtil.isNullOrBlank(serviceName)) {
					serviceName = clazz.getSimpleName();
				}
				GenericServlet servlet = (GenericServlet) clazz.newInstance();
				servlets.put(serviceName, servlet);
				servlet.setConfig(config);
			} catch (ClassNotFoundException e) {
				logger.error("[MTPServer] 不存在[ " + className + " ]", e);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
		return servlets;
	}

	Map<String, GenericServlet> loadServlets(ServerContext context) throws Exception {
		return this.loadServlets(context, "conf/servlets.config");
	}

	Map<String, GenericServlet> loadServlets(ServerContext context, String configPath) throws Exception {
		String config = FileUtil.readContentByCls(configPath, Encoding.UTF8);
		if (!StringUtil.isNullOrBlank(config)) {
			logger.info("[MTPServer] 读取Servlet配置文件");
			JSONArray array = JSONObject.parseArray(config);
			return loadServlets(array);
		} else {
			throw new Exception("[MTPServer] 不存在Servlet配置文件");
		}
	}

	private Map<String, Object> reflectMap(JSONObject jsonObject) {
		try {
			Field field = jsonObject.getClass().getDeclaredField("map");
			field.setAccessible(true);
			Map<String, Object> map = (Map<String, Object>) field.get(jsonObject);
			field.setAccessible(false);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private void destroyServlets(Map<String, GenericServlet> servlets) throws Exception {
		synchronized (servlets) {
			Set<Entry<String, GenericServlet>> entries = servlets.entrySet();
			for (Entry<String, GenericServlet> entry : entries) {
				GenericServlet servlet = entry.getValue();
				try {
					logger.info("[MTPServer] 卸载完成："+servlet);
					servlet.destroy(context, servlet.getConfig());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public boolean redeploy(DynamicClassLoader classLoader) {
		Map<String, GenericServlet> servlets;
		Map<String, GenericServlet> _servlets = this.servlets;
		try {
			servlets = loadServlets(context);
			this.initializeServlets(servlets);
			this.servlets = servlets;
			this.destroyServlets(_servlets);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
