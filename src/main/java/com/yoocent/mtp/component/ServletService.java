package com.yoocent.mtp.component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yoocent.mtp.AbstractLifeCycle;
import com.yoocent.mtp.FlushedException;
import com.yoocent.mtp.LifeCycle;
import com.yoocent.mtp.common.FileUtil;
import com.yoocent.mtp.common.LifeCycleUtil;
import com.yoocent.mtp.common.StringUtil;
import com.yoocent.mtp.server.Request;
import com.yoocent.mtp.server.Response;
import com.yoocent.mtp.server.ServletAcceptAble;
import com.yoocent.mtp.server.context.ServletContext;
import com.yoocent.mtp.servlet.GenericServlet;
import com.yoocent.mtp.servlet.MTPFilterService;
import com.yoocent.mtp.servlet.MTPFilterServiceImpl;
import com.yoocent.mtp.servlet.impl.ErrorServlet;
import com.yoocent.mtp.servlet.impl.StopServerServlet;

public final class ServletService extends AbstractLifeCycle implements ServletAcceptAble , LifeCycle{

	private ServletContext context = null;
	
	public ServletService(ServletContext context) {
		this.context = context;
	}
	
	private MTPFilterService service = null;

	private Map<String, GenericServlet> servlets = new HashMap<String, GenericServlet>();
	
	private Map<String, GenericServlet> errorServlets = new HashMap<String, GenericServlet>();
	
	public void accept(Request request, Response response) throws IOException{
		try {
			//TODO
			if (service.doFilter(request, response)) {
				return;
			}
		} catch (FlushedException e) {
			e.printStackTrace();
		} catch (ChannelException e) {
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
			this.acceptException(e, request, response);
		} catch (Exception e) {
			e.printStackTrace();
			this.acceptException(e, request, response);
		}
		
		this.acceptServlet(request, response);
	}
	
	public void acceptServlet(Request request, Response response) throws IOException {
		String serviceName = request.getServiceName();
		if (StringUtil.isBlankOrNull(serviceName)) {
			this.accept404(request, response);
		}else{
			this.acceptNormal(serviceName,request, response);
		}
	}
	
	private void acceptNormal(String serviceName,Request request, Response response) throws IOException  {
		ServletAcceptAble servlet = servlets.get(serviceName);
		if (servlet == null) {
			servlet = this.errorServlets.get(serviceName);
			if (servlet == null) {
				this.accept404(request, response);
			}else{
				try {
					servlet.accept(request, response);
				} catch (FlushedException e) {
					e.printStackTrace();
				} catch (ChannelException e) {
					e.printStackTrace();
				} catch (IOException e){
					e.printStackTrace();
					this.acceptException(e, request, response);
				} catch (Exception e) {
					e.printStackTrace();
					this.acceptException(e, request, response);
				}
			}
		}else{
			try {
				servlet.accept(request, response);
			} catch (FlushedException e) {
				e.printStackTrace();
			} catch (ChannelException e) {
				e.printStackTrace();
			} catch (IOException e){
				e.printStackTrace();
				this.acceptException(e, request, response);
			} catch (Exception e) {
				e.printStackTrace();
				this.acceptException(e, request, response);
			}
		}
	}
	
	private void acceptException(Exception exception,Request request, Response response) throws IOException{
		ErrorServlet servlet = new ErrorServlet(exception);
		try {
			servlet.accept(request, response);
		} catch(IOException e){
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void doStart() throws Exception {
		this.loadServlets(context);
		this.servlets.put(StopServerServlet.SERVICE_KEY, new StopServerServlet());
		this.service = new MTPFilterServiceImpl(context,this);
		this.service.start();
		this.initialize();
		
	}
	
	
	private void initialize(){
		Set<Entry<String, GenericServlet>> entries = servlets.entrySet();
		for(Entry<String, GenericServlet> entry : entries){
			GenericServlet servlet = entry.getValue();
			try {
				servlet.start();
			} catch (Exception e) {
				e.printStackTrace();
				String serviceName = entry.getKey();
				ErrorServlet error = new ErrorServlet(e);
				this.errorServlets.put(serviceName, error);
			}
		}
		
		Set<String> errorKeys = errorServlets.keySet();
		for(String key : errorKeys){
			this.servlets.remove(key);
		}
	}
	
	
	private void loadServlets(ServletContext context) {
		try {
			String str = FileUtil.readContentByCls("servlets.config", "UTF-8");
			JSONArray jArray = JSONObject.parseArray(str);
			for (int i = 0; i < jArray.size(); i++) {
				JSONObject jObj = jArray.getJSONObject(i);
				String clazz = jObj.getString("class");
				String serviceName = jObj.getString("serviceName");
				ServletConfig config = new ServletConfig();
				Map<String, Object> map = toMap(jObj);
				config.setConfig(map);
				try {
					GenericServlet servlet = (GenericServlet) Class.forName(
							clazz).newInstance();
					this.servlets.put(serviceName, servlet);
					servlet.setConfig(config);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static Map<String, Object> toMap(JSONObject jsonObject) {
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
	

	protected void doStop() throws Exception {
		this.service.stop();
		synchronized (servlets) {
			Set<Entry<String, GenericServlet>> entries = servlets.entrySet();
			for(Entry<String, GenericServlet> entry : entries){
				GenericServlet servlet = entry.getValue();
				try {
					LifeCycleUtil.stop(servlet);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void accept404(Request request,Response response) throws IOException{
		String serviceName = request.getServiceName();
		System.out.println("[MTPServer] 未发现命令："+serviceName);
		response.write("404 not found service :".getBytes());
		if (!StringUtil.isBlankOrNull(serviceName)) {
			response.write(request.getServiceName().getBytes());
		}
		response.flush();
	}

}
