package com.gifisan.mtp.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.LifeCycle;
import com.gifisan.mtp.common.FileUtil;
import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.component.FilterConfig;
import com.gifisan.mtp.component.ServletService;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.context.ServletContext;

public final class MTPFilterServiceImpl extends AbstractLifeCycle implements MTPFilterService , LifeCycle{
	
	private ServletContext context = null;
	
	private ServletService service = null;
	
	private boolean useFilters = false;
	
	private List<WrapperMTPFilter> filters = new ArrayList<WrapperMTPFilter>();

	public MTPFilterServiceImpl(ServletContext context, ServletService service) {
		this.context = context;
		this.service = service;
	}

	public boolean doFilter(Request request, Response response)throws Exception {
		if (useFilters) {
			for(WrapperMTPFilter filter : filters){
				boolean _break = filter.doFilter(request, response);
				if (_break) {
					return true;
				}
			}
			return false;
		}
		return false;
	}

	public void accept(Request request, Response response) throws Exception {
		this.service.acceptServlet(request, response);
		
	}
	
	private void loadFilters (){
		try {
			String str = FileUtil.readContentByCls("filters.config", "UTF-8");
			if (StringUtil.isBlankOrNull(str)) {
				
				return ;
			}
			
			JSONArray jArray = JSONArray.parseArray(str);
			
			if (jArray.size() > 0) {
				useFilters = true;
				for (int i = 0; i < jArray.size(); i++) {
					JSONObject jObj = jArray.getJSONObject(i);
					String clazz = jObj.getString("class");
					Map<String,Object> config = toMap(jObj);
					FilterConfig filterConfig = new FilterConfig();
					filterConfig.setConfig(config);
					try {
						MTPFilter filter =(MTPFilter)Class.forName(clazz).newInstance();
						this.filters.add(new WrapperMTPFilterImpl(context, filter, filterConfig));
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
				}
			}
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
	protected void doStart() throws Exception {
		this.loadFilters();
		//start all filter
		for (int i = 0; i < filters.size(); i++) {
			WrapperMTPFilter filter = filters.get(i);
			try {
				filter.start();
			} catch (Exception e) {
				e.printStackTrace();
				filters.remove(i);
				i--;
			}
		}
		
	}

	protected void doStop() throws Exception {
		for(WrapperMTPFilter filter : filters){
			try {
				LifeCycleUtil.stop(filter);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
