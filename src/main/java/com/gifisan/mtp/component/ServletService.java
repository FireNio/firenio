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
import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.Encoding;
import com.gifisan.mtp.LifeCycle;
import com.gifisan.mtp.common.FileUtil;
import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.common.SharedBundle;
import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.ServletAcceptor;
import com.gifisan.mtp.server.ServletContext;
import com.gifisan.mtp.servlet.GenericServlet;
import com.gifisan.mtp.servlet.MTPFilterService;
import com.gifisan.mtp.servlet.MTPFilterServiceImpl;
import com.gifisan.mtp.servlet.impl.ErrorServlet;
import com.gifisan.mtp.servlet.impl.StopServerServlet;

public final class ServletService extends AbstractLifeCycle implements ServletAcceptor, LifeCycle {

	private ServletContext				context		= null;
	private Map<String, GenericServlet>	errorServlets	= new HashMap<String, GenericServlet>();
	private MTPFilterService				filterService	= null;
	private final Logger				logger		= LoggerFactory.getLogger(ServletService.class);
	private Map<String, GenericServlet>	servlets		= new LinkedHashMap<String, GenericServlet>();

	public ServletService(ServletContext context) {
		this.context = context;
	}

	public void accept(Request request, Response response) throws IOException {
		try {
			if (filterService.doFilter(request, response)) {
				return;
			}
		} catch (FlushedException e) {
			e.printStackTrace();
		} catch (MTPChannelException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			this.acceptException(e, request, response);
		} catch (Exception e) {
			e.printStackTrace();
			this.acceptException(e, request, response);
		}

		String serviceName = request.getServiceName();
		this.accept(request, response, serviceName);
	}

	private void accept(Request request, Response response, String serviceName) throws IOException {
		if (StringUtil.isNullOrBlank(serviceName)) {
			this.accept404(request, response);

		} else {
			this.acceptNormal(serviceName, request, response);
		}
	}

	private void accept404(Request request, Response response) throws IOException {
		logger.info("[MTPServer] empty service name");
		response.write(RESMessage.R404_EMPTY.toString().getBytes(Encoding.DEFAULT));
		response.flush();
	}

	private void accept404(Request request, Response response, String serviceName) throws IOException {
		logger.info("[MTPServer] 未发现命令：" + serviceName);
		RESMessage message = new RESMessage(404, "service name not found :" + serviceName);
		response.write(message.toString());
		response.flush();
	}

	private void acceptException(Exception exception, Request request, Response response) throws IOException {
		ErrorServlet servlet = new ErrorServlet(exception);
		try {
			servlet.accept(request, response);
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void acceptNormal(String serviceName, Request request, Response response) throws IOException {
		ServletAcceptor servlet = servlets.get(serviceName);
		if (servlet == null) {
			servlet = this.errorServlets.get(serviceName);
			if (servlet == null) {
				this.accept404(request, response, serviceName);
			} else {
				this.acceptNormal0(servlet, request, response);
			}
		} else {
			this.acceptNormal0(servlet, request, response);
		}
	}

	private void acceptNormal0(ServletAcceptor servlet, Request request, Response response) throws IOException {
		try {
			servlet.accept(request, response);
		} catch (FlushedException e) {
			e.printStackTrace();
		} catch (MTPChannelException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			this.acceptException(e, request, response);
		} catch (Exception e) {
			e.printStackTrace();
			this.acceptException(e, request, response);
		}

	}

	protected void doStart() throws Exception {
		this.loadServlets(context);
		this.servlets.put(StopServerServlet.SERVICE_NAME, new StopServerServlet());
		this.filterService = new MTPFilterServiceImpl(context);
		this.filterService.start();
		this.initialize();

	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(filterService);
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

	private void initialize() {
		Set<Entry<String, GenericServlet>> entries = servlets.entrySet();
		for (Entry<String, GenericServlet> entry : entries) {
			GenericServlet servlet = entry.getValue();
			try {
				servlet.start();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				String serviceName = entry.getKey();
				ErrorServlet error = new ErrorServlet(e);
				this.errorServlets.put(serviceName, error);
			}
		}

		Set<String> errorKeys = errorServlets.keySet();
		for (String key : errorKeys) {
			this.servlets.remove(key);
		}
	}

	private void loadServlets(ServletContext context) throws Exception {
		try {
			boolean read = false;
			String config = FileUtil.readContentByCls("servlets.config", Encoding.UTF8);
			if (!StringUtil.isNullOrBlank(config)) {
				logger.info("[MTPServer] 读取默认Servlet配置文件");
				JSONArray array = JSONObject.parseArray(config);
				loadServlets(array);
				read = true;
			}else{
				logger.warn("[MTPServer] 不存在默认的Servlet配置文件");
			}
			
			String userConfigPath = SharedBundle.instance().getProperty("SERVER.SERVLETS");
			
			if (!StringUtil.isNullOrBlank(userConfigPath)) {
				File userConfigFile = new File(userConfigPath);
				if (userConfigFile.exists()) {
					String userConfig = FileUtil.readFileToString(userConfigFile, Encoding.UTF8);
					if (StringUtil.isNullOrBlank(userConfig)) {
						logger.warn("[MTPServer] 不存在自定义的Servlet配置文件："+userConfigFile.getAbsolutePath());
						if (!read) {
							throw new Exception("没有配置任何Servlet");
						}
					}else{
						logger.info("[MTPServer] 读取自定义Servlet配置文件："+userConfigFile.getAbsolutePath());
						JSONArray array = JSONObject.parseArray(userConfig);
						loadServlets(array);
					}
				}else{
					logger.warn("[MTPServer] 不存在自定义的Servlet配置文件："+userConfigFile.getAbsolutePath());
					if (!read) {
						throw new Exception("没有配置任何Servlet");
					}
				}
				
				
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadServlets(JSONArray array){
		for (int i = 0; i < array.size(); i++) {
			JSONObject object = array.getJSONObject(i);
			String clazz = object.getString("class");
			String serviceName = object.getString("serviceName");
			ServletConfig config = new ServletConfig();
			Map<String, Object> map = toMap(object);
			config.setConfig(map);
			try {
				GenericServlet servlet = (GenericServlet) Class.forName(clazz).newInstance();
				this.servlets.put(serviceName, servlet);
				servlet.setConfig(config);
			} catch (ClassNotFoundException e) {
				logger.error("[MTPServer] 不存在[ " + clazz + " ]", e);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
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

}
