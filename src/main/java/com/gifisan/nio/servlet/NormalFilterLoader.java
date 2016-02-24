package com.gifisan.nio.servlet;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.Encoding;
import com.gifisan.nio.common.FileUtil;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.DynamicClassLoader;
import com.gifisan.nio.component.ServletFilter;
import com.gifisan.nio.server.ServerContext;

public class NormalFilterLoader extends AbstractLifeCycle implements FilterLoader {

	private Logger				logger		= LoggerFactory.getLogger(NormalFilterLoader.class);
	private NIOFilterWrapper		rootFilter	= null;
	private ServerContext		context		= null;
	private DynamicClassLoader	classLoader	= null;

	public NormalFilterLoader(ServerContext context, DynamicClassLoader classLoader) {
		this.context = context;
		this.classLoader = classLoader;
	}

	private NIOFilterWrapper loadFilters(ServerContext context, DynamicClassLoader classLoader) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		
		String config = FileUtil.readContentByCls("conf/filters.config", Encoding.DEFAULT);
		
		if (StringUtil.isNullOrBlank(config)) {
			
			logger.info("[NIOServer] 没有配置Filter");
			
			NIOFilterWrapperImpl filter = new NIOFilterWrapperImpl(context, new ServletFilter(classLoader), null);
			
			return filter;
			
		} else {
			
			JSONArray array = JSONArray.parseArray(config);
			
			return loadFilters(context, classLoader, array);
		}
	}

	public NIOFilterWrapper getRootFilter() {
		return rootFilter;
	}

	private NIOFilterWrapper loadFilters(ServerContext context, DynamicClassLoader classLoader, JSONArray array) throws InstantiationException, IllegalAccessException, ClassNotFoundException {

		NIOFilterWrapper rootFilter = null;

		NIOFilterWrapper last = null;

		for (int i = 0; i < array.size(); i++) {
			
			JSONObject object = array.getJSONObject(i);
			
			String clazz = object.getString("class");
			
			Configuration filterConfig = new Configuration(object);
			

			NIOFilter filter = (NIOFilter) classLoader.forName(clazz).newInstance();

			NIOFilterWrapperImpl _filter = new NIOFilterWrapperImpl(context, filter, filterConfig);

			if (last == null) {
				
				last = _filter;
				
				rootFilter = _filter;
				
			} else {
				
				last.setNextFilter(_filter);
				
			}

		}

		NIOFilterWrapperImpl filter = new NIOFilterWrapperImpl(context, new ServletFilter(classLoader), null);

		if (last == null) {
			rootFilter = filter;
		} else {
			last.setNextFilter(filter);
		}

		return rootFilter;
	}


	protected void doStart() throws Exception {
		this.rootFilter = this.loadFilters(context, classLoader);

		// start all filter
		this.initializeFilters(rootFilter);
	}

	private void initializeFilters(NIOFilterWrapper filter) throws Exception {
		for (; filter != null;) {
			filter.start();
			filter = filter.nextFilter();
		}
	}

	private void destroyFilters(NIOFilterWrapper filter) {
		for (; filter != null;) {
			LifeCycleUtil.stop(filter);
			filter = filter.nextFilter();
		}
	}

	protected void doStop() throws Exception {
		this.destroyFilters(rootFilter);
	}

	public void redeploy(DynamicClassLoader classLoader) {
		
		context.setAttribute("_OLD_FILTERS", rootFilter);
		
		this.rootFilter = (NIOFilterWrapper) context.getAttribute("_NEW_FILTERS");
		
		logger.info("[NIOServer] 新的Filter配置文件替换完成......");
	}

	private void predeploy0(NIOFilterWrapper filter) throws Exception {
		
		for (; filter != null;) {
			
			filter.onPreDeploy(context, filter.getConfig());
			
			logger.info("[NIOServer] 新的Filter [ {} ] 更新完成",filter);
			
			filter = filter.nextFilter();
		}
	}

	private void predeploy0(DynamicClassLoader classLoader) throws Exception {
		
		logger.info("[NIOServer] 尝试加载新的Filter配置文件......");
		
		NIOFilterWrapper rootFilter = null;
		
		rootFilter = loadFilters(context, classLoader);
		
		logger.info("[NIOServer] 尝试更新新的Filter配置文件......");
		
		this.predeploy0(rootFilter);
		
		context.setAttribute("_NEW_FILTERS", rootFilter);
		
		logger.info("[NIOServer] 新的Filter配置文件加载完成......");
		
	}

	public boolean predeploy(DynamicClassLoader classLoader) {
		
		try {
			
			this.predeploy0(classLoader);
			
			return true;
			
		} catch (Exception e) {
			
			logger.error(e.getMessage(),e);
			
			return false;
		}
	}

	private void subdeploy(NIOFilterWrapper filter) {
		for (; filter != null;) {
			try {
				filter.onSubDeploy(context, filter.getConfig());
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
			filter = filter.nextFilter();
		}
	}

	private void subdeploy0(DynamicClassLoader classLoader) {
		context.removeAttribute("_NEW_FILTERS");
		NIOFilterWrapper filter = (NIOFilterWrapper) context.removeAttribute("_OLD_FILTERS");
		try {
			subdeploy(filter);
		} catch (Exception e) {
			// ignore
			logger.error(e.getMessage(),e);
		}
	}

	public void subdeploy(DynamicClassLoader classLoader) {
		this.subdeploy0(classLoader);
//		this.servletFilter.subdeploy(classLoader);
	}

}
