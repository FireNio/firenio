package com.gifisan.mtp.servlet;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.Encoding;
import com.gifisan.mtp.common.FileUtil;
import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.component.DynamicClassLoader;
import com.gifisan.mtp.component.FilterConfig;
import com.gifisan.mtp.component.ServletFilter;
import com.gifisan.mtp.server.ServerContext;

public class NormalFilterLoader extends AbstractLifeCycle implements FilterLoader {

	private Logger				logger			= LoggerFactory.getLogger(NormalFilterLoader.class);
	MTPFilterWrapper		rootFilter		= null;
	private ServerContext		context			= null;
	private DynamicClassLoader	classLoader		= null;

	public NormalFilterLoader(ServerContext context, DynamicClassLoader classLoader) {
		this.context = context;
		this.classLoader = classLoader;
	}
	
	void loadFilters(ServerContext context,String configPath) {
		try {
			String config = FileUtil.readContentByCls(configPath, Encoding.DEFAULT);
			if (StringUtil.isNullOrBlank(config)) {
				logger.info("[MTPServer] 没有配置Filter");
			} else {
				logger.info("[MTPServer] 读取Filter配置文件");
				JSONArray array = JSONArray.parseArray(config);
				loadFilters(context, array);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	void loadFilters(ServerContext context) {
		this.loadFilters(context, "conf/filters.config");
	}

	public MTPFilterWrapper getRootFilter() {
		return rootFilter;
	}

	void loadFilters(ServerContext context, JSONArray array) throws IOException {

		MTPFilterWrapper last = null;

		for (int i = 0; i < array.size(); i++) {
			JSONObject jObj = array.getJSONObject(i);
			String clazz = jObj.getString("class");
			Map<String, Object> config = reflectMap(jObj);
			FilterConfig filterConfig = new FilterConfig();
			filterConfig.setConfig(config);
			try {

				MTPFilter filter = (MTPFilter) classLoader.forName(clazz).newInstance();

				MTPFilterWrapperImpl _filter = new MTPFilterWrapperImpl(context, filter, filterConfig);

				if (last == null) {
					last = _filter;
					rootFilter = _filter;
				} else {
					last.setNextFilter(_filter);
				}

			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
		
		MTPFilterWrapperImpl filter = new MTPFilterWrapperImpl(context,new ServletFilter(),null);
		
		if (last == null) {
			rootFilter = filter;
		}else{
			last.setNextFilter(filter);
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

	//TODO .....
	protected void doStart() throws Exception {
		this.loadFilters(context);
		
		

		// start all filter
		this.initializeFilters();
	}

	private void initializeFilters() {
		MTPFilterWrapper filter = rootFilter;
		MTPFilterWrapper preFilter = null;
		for (; filter != null;) {
			try {
				filter.start();
				filter = filter.nextFilter();
				preFilter = filter;
			} catch (Exception e) {
				e.printStackTrace();
				MTPFilterWrapper _next = filter.nextFilter();
				if (preFilter != null) {
					preFilter.setNextFilter(_next);
				} else {
					rootFilter = filter;
				}
				filter = _next;
			}
		}
	}

	private void destroyFilters() {
		MTPFilterWrapper filter = rootFilter;
		for (; filter != null;) {
			try {
				LifeCycleUtil.stop(filter);
			} catch (Exception e) {
				e.printStackTrace();
			}
			filter = filter.nextFilter();
		}
	}

	protected void doStop() throws Exception {
		this.destroyFilters();
	}

	public boolean redeploy(DynamicClassLoader classLoader) {
		this.classLoader = classLoader;
		
		try {
			this.doStart();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

}
