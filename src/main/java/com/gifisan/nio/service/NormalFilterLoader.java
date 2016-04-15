package com.gifisan.nio.service;

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
			
			logger.info(" [NIOServer] 没有配置Filter");
			
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

	private void prepare(NIOFilterWrapper filter) throws Exception {
		
		for (; filter != null ;) {
			
			filter.prepare(context, filter.getConfig());
			
			logger.info(" [NIOServer] 新的Filter  [ {} ] Prepare完成",filter);
			
			filter = filter.nextFilter();
		}
	}

	public void prepare(ServerContext context, Configuration config) throws Exception {
		
		logger.info(" [NIOServer] 尝试加载新的Filter配置......");
		
		this.rootFilter = loadFilters(context, classLoader);
		
		logger.info(" [NIOServer] 尝试启动新的Filter配置......");
		
		this.prepare(rootFilter);
		
	}

	public void unload(ServerContext context, Configuration config) throws Exception {
		
		NIOFilterWrapper filter = rootFilter;
		
		for (; filter != null;) {
			
			try {
				filter.unload(context, filter.getConfig());
				
				logger.info(" [NIOServer] 旧的Filter  [ {} ] Unload完成",filter);
				
			} catch (Throwable e) {
				// ignore
				logger.info(" [NIOServer] 旧的Filter  [ {} ] Unload失败",filter);
				logger.error(e.getMessage(),e);
			}
			
			filter = filter.nextFilter();
			
		}
	}

}
