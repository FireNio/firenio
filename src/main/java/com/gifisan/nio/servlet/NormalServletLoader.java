package com.gifisan.nio.servlet;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.Encoding;
import com.gifisan.nio.common.FileUtil;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.DynamicClassLoader;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.server.ServletAcceptor;
import com.gifisan.nio.servlet.impl.ErrorServlet;

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
		Map<String, GenericServlet> servlets = loadServlets(context, this.classLoader);

		this.initializeServlets(servlets);

		this.servlets = servlets;

	}

	protected void doStop() throws Exception {
		this.destroyServlets(this.servlets);
	}

	public ServletAcceptor getServlet(String serviceName) {
		return servlets.get(serviceName);
	}

	private void initializeServlets(Map<String, GenericServlet> servlets) throws Exception{
		Set<Entry<String, GenericServlet>> entries = servlets.entrySet();
		for (Entry<String, GenericServlet> entry : entries) {
			GenericServlet servlet = entry.getValue();
			servlet.initialize(context, servlet.getConfig());
			logger.info("[NIOServer] 加载完成：{}" , servlet);
		}
	}

	private Map<String, GenericServlet> loadServlets(JSONArray array, DynamicClassLoader classLoader) throws Exception {
		Map<String, GenericServlet> servlets = new LinkedHashMap<String, GenericServlet>();
		if (array.size() == 0) {
			throw new Exception("empty servlet config");
		}
		for (int i = 0; i < array.size(); i++) {
			
			JSONObject object = array.getJSONObject(i);
			
			String className = object.getString("class");
			
			String serviceName = object.getString("serviceName");
			
			Configuration config = new Configuration(object);
			
			Class<?> clazz = classLoader.forName(className);
			
			if (StringUtil.isNullOrBlank(serviceName)) {
				
				serviceName = clazz.getSimpleName();
				
			}
			
			GenericServlet servlet = (GenericServlet) clazz.newInstance();
			
			servlets.put(serviceName, servlet);
			
			servlet.setConfig(config);
			
		}
		return servlets;
	}

	private Map<String, GenericServlet> loadServlets(ServerContext context, DynamicClassLoader classLoader)
			throws Exception {
		String config = FileUtil.readContentByCls("conf/servlets.config", Encoding.UTF8);
		if (!StringUtil.isNullOrBlank(config)) {
			JSONArray array = JSONObject.parseArray(config);
			return loadServlets(array, classLoader);
		} else {
			throw new Exception("[NIOServer] 不存在Servlet配置文件");
		}
	}

	private void destroyServlets(Map<String, GenericServlet> servlets) throws Exception {
		synchronized (servlets) {
			Set<Entry<String, GenericServlet>> entries = servlets.entrySet();
			for (Entry<String, GenericServlet> entry : entries) {
				GenericServlet servlet = entry.getValue();
				try {
					logger.info("[NIOServer] 卸载完成：" + servlet);
					servlet.destroy(context, servlet.getConfig());
				} catch (Exception e) {
					logger.error(e.getMessage(),e);
				}
			}
		}
	}

	public void redeploy(DynamicClassLoader classLoader) {
		context.setAttribute("_OLD_SERVLETS", servlets);
		this.servlets = (Map<String, GenericServlet>) context.getAttribute("_NEW_SERVLETS");
		logger.info("[NIOServer] 新的Servlet配置文件替换完成......");
	}

	private void predeploy(Map<String, GenericServlet> servlets) {
		Set<Entry<String, GenericServlet>> entries = servlets.entrySet();
		for (Entry<String, GenericServlet> entry : entries) {
			GenericServlet servlet = entry.getValue();
			try {
				servlet.onPreDeploy(context, servlet.getConfig());
				logger.info("[NIOServer] 新的Servlet [ {} ] 更新完成" ,servlet);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				String serviceName = entry.getKey();
				ErrorServlet error = new ErrorServlet(e);
				servlets.put(serviceName, error);
			}
		}
	}

	public boolean predeploy(DynamicClassLoader classLoader){
		Map<String, GenericServlet> servlets;
		try {
			logger.info("[NIOServer] 尝试加载新的Servlet配置文件......");
			servlets = loadServlets(context, classLoader);
			logger.info("[NIOServer] 尝试更新新的Servlet配置文件......");
			this.predeploy(servlets);
			context.setAttribute("_NEW_SERVLETS", servlets);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			logger.info("[NIOServer] 新的Servlet配置文件加载失败，将停止部署......");
			return false;
		}
		logger.info("[NIOServer] 新的Servlet配置文件加载完成......");
		return true;
	}

	private void subdeploy(Map<String, GenericServlet> servlets) throws Exception {
		synchronized (servlets) {
			Set<Entry<String, GenericServlet>> entries = servlets.entrySet();
			for (Entry<String, GenericServlet> entry : entries) {
				GenericServlet servlet = entry.getValue();
				try {
					servlet.onSubDeploy(context, servlet.getConfig());
					logger.info("[NIOServer] 替换完成：" + servlet);
				} catch (Exception e) {
					logger.error(e.getMessage(),e);
				}
			}
		}
	}

	public void subdeploy(DynamicClassLoader classLoader) {
		context.removeAttribute("_NEW_SERVLETS");
		Map<String, GenericServlet> servlets = (Map<String, GenericServlet>) context.removeAttribute("_OLD_SERVLETS");
		try {
			subdeploy(servlets);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}

}
