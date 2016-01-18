package com.gifisan.mtp.component;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gifisan.mtp.Encoding;
import com.gifisan.mtp.common.FileUtil;
import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.common.SharedBundle;
import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.server.ServletAcceptor;
import com.gifisan.mtp.server.ServletContext;
import com.gifisan.mtp.servlet.GenericServlet;
import com.gifisan.mtp.servlet.impl.ErrorServlet;

public class ServletLoader {

	private final Logger				logger	= LoggerFactory.getLogger(ServletLoader.class);
	private Map<String, GenericServlet>	servlets	= new LinkedHashMap<String, GenericServlet>();

	public ServletAcceptor getServlet(String serviceName) {
		return servlets.get(serviceName);
	}

	public void initialize() {
		Set<Entry<String, GenericServlet>> entries = servlets.entrySet();
		for (Entry<String, GenericServlet> entry : entries) {
			GenericServlet servlet = entry.getValue();
			try {
				servlet.start();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				String serviceName = entry.getKey();
				ErrorServlet error = new ErrorServlet(e);
				this.servlets.put(serviceName, error);
			}
		}
	}

	private void loadServlets(JSONArray array) {
		for (int i = 0; i < array.size(); i++) {
			JSONObject object = array.getJSONObject(i);
			String className = object.getString("class");
			String serviceName = object.getString("serviceName");
			ServletConfig config = new ServletConfig();
			Map<String, Object> map = toMap(object);
			config.setConfig(map);
			try {
				Class<?> clazz = Class.forName(className);
				if (StringUtil.isNullOrBlank(serviceName)) {
					serviceName = clazz.getSimpleName();
				}
				GenericServlet servlet = (GenericServlet) clazz.newInstance();
				this.servlets.put(serviceName, servlet);
				servlet.setConfig(config);
			} catch (ClassNotFoundException e) {
				logger.error("[MTPServer] 不存在[ " + className + " ]", e);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
	}

	private void loadUserConfig(boolean read) throws Exception {
		String userConfigPath = SharedBundle.instance().getProperty("SERVER.SERVLETS");
		if (!StringUtil.isNullOrBlank(userConfigPath)) {
			File userConfigFile = new File(userConfigPath);
			if (userConfigFile.exists()) {
				String userConfig = FileUtil.readFileToString(userConfigFile, Encoding.UTF8);
				if (StringUtil.isNullOrBlank(userConfig)) {
					logger.warn("[MTPServer] 不存在自定义的Servlet配置文件：" + userConfigFile.getAbsolutePath());
					if (!read) {
						throw new Exception("没有配置任何Servlet");
					}
				} else {
					logger.info("[MTPServer] 读取自定义Servlet配置文件：" + userConfigFile.getAbsolutePath());
					JSONArray array = JSONObject.parseArray(userConfig);
					loadServlets(array);
				}
			} else {
				logger.warn("[MTPServer] 不存在自定义的Servlet配置文件：" + userConfigFile.getAbsolutePath());
				if (!read) {
					throw new Exception("没有配置任何Servlet");
				}
			}
		}
	}

	public void loadServlets(ServletContext context) throws Exception {
		try {
			boolean read = false;
			String config = FileUtil.readContentByCls("servlets.config", Encoding.UTF8);
			if (!StringUtil.isNullOrBlank(config)) {
				logger.info("[MTPServer] 读取默认Servlet配置文件");
				JSONArray array = JSONObject.parseArray(config);
				loadServlets(array);
				read = true;
			} else {
				logger.warn("[MTPServer] 不存在默认的Servlet配置文件");
			}
			this.loadUserConfig(read);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Map<String, Object> toMap(JSONObject jsonObject) {
		Map<String, Object> result = new HashMap<String, Object>();
		Set enteys = jsonObject.entrySet();
		Iterator iterator = enteys.iterator();
		while (iterator.hasNext()) {
			Entry e = (Entry) iterator.next();
			String key = (String) e.getKey();
			Object value = e.getValue();
			result.put(key, value);
		}
		return result;
	}

	public void unloadServlets() throws Exception {
		synchronized (servlets) {
			Set<Entry<String, GenericServlet>> entries = servlets.entrySet();
			for (Entry<String, GenericServlet> entry : entries) {
				GenericServlet servlet = entry.getValue();
				try {
					LifeCycleUtil.stop(servlet);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
