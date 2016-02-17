package com.gifisan.mtp.servlet;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.Encoding;
import com.gifisan.mtp.common.FileUtil;
import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.component.Configuration;
import com.gifisan.mtp.component.DynamicClassLoader;
import com.gifisan.mtp.component.ServletFilter;
import com.gifisan.mtp.server.ServerContext;

public class NormalFilterLoader extends AbstractLifeCycle implements FilterLoader {

	private Logger				logger		= LoggerFactory.getLogger(NormalFilterLoader.class);
	private MTPFilterWrapper		rootFilter	= null;
	private ServerContext		context		= null;
	private DynamicClassLoader	classLoader	= null;
//	private ServletFilter		servletFilter	= null;

	public NormalFilterLoader(ServerContext context, DynamicClassLoader classLoader) {
		this.context = context;
		this.classLoader = classLoader;
//		this.servletFilter = new ServletFilter(classLoader);
	}

	private MTPFilterWrapper loadFilters(ServerContext context, DynamicClassLoader classLoader) throws IOException {
		String config = FileUtil.readContentByCls("conf/filters.config", Encoding.DEFAULT);
		if (StringUtil.isNullOrBlank(config)) {
			logger.info("[MTPServer] 没有配置Filter");
			MTPFilterWrapperImpl filter = new MTPFilterWrapperImpl(context, new ServletFilter(classLoader), null);
			return filter;
		} else {
			JSONArray array = JSONArray.parseArray(config);
			return loadFilters(context, classLoader, array);
		}
	}

	public MTPFilterWrapper getRootFilter() {
		return rootFilter;
	}

	private MTPFilterWrapper loadFilters(ServerContext context, DynamicClassLoader classLoader, JSONArray array) {

		MTPFilterWrapper rootFilter = null;

		MTPFilterWrapper last = null;

		for (int i = 0; i < array.size(); i++) {
			JSONObject object = array.getJSONObject(i);
			String clazz = object.getString("class");
			Configuration filterConfig = new Configuration(object);
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
				logger.error(e.getMessage(),e);
				continue;
			}
		}

		MTPFilterWrapperImpl filter = new MTPFilterWrapperImpl(context, new ServletFilter(classLoader), null);

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

	private void initializeFilters(MTPFilterWrapper filter) throws Exception {
		for (; filter != null;) {
			filter.start();
			filter = filter.nextFilter();
		}
	}

	private void destroyFilters(MTPFilterWrapper filter) {
		for (; filter != null;) {
			LifeCycleUtil.stop(filter);
			filter = filter.nextFilter();
		}
	}

	protected void doStop() throws Exception {
		this.destroyFilters(rootFilter);
	}

	private void redeploy0(DynamicClassLoader classLoader) {
		context.setAttribute("_OLD_FILTERS", rootFilter);
		this.rootFilter = (MTPFilterWrapper) context.getAttribute("_NEW_FILTERS");
		logger.info("[MTPServer] 新的Filter配置文件替换完成......");
	}

	public void redeploy(DynamicClassLoader classLoader) {
		this.redeploy0(classLoader);
//		this.servletFilter.redeploy(classLoader);
	}

	private boolean predeploy0(MTPFilterWrapper filter) {
		for (; filter != null;) {
			try {
				filter.onPreDeploy(context, filter.getConfig());
				logger.info("[MTPServer] 新的Filter [ {} ] 更新完成",filter);
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
				return false;
			}
			filter = filter.nextFilter();
		}
		return true;
	}

	private boolean predeploy0(DynamicClassLoader classLoader) {
		logger.info("[MTPServer] 尝试加载新的Filter配置文件......");
		MTPFilterWrapper rootFilter;
		try {
			rootFilter = loadFilters(context, classLoader);
		} catch (IOException e) {
			logger.info("[MTPServer] 新的Filter配置文件加载失败，将停止部署......");
			logger.error(e.getMessage(),e);
			return false;
		}
		logger.info("[MTPServer] 尝试更新新的Filter配置文件......");
		if (predeploy0(rootFilter)) {
			context.setAttribute("_NEW_FILTERS", rootFilter);
			return true;
		}
		logger.info("[MTPServer] 新的Filter配置文件加载完成......");
		return false;

	}

	public boolean predeploy(DynamicClassLoader classLoader) {
		if (predeploy0(classLoader)) {
//			return this.servletFilter.predeploy(classLoader);
			return true;
		}
		return false;
	}

	private void subdeploy(MTPFilterWrapper filter) {
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
		MTPFilterWrapper filter = (MTPFilterWrapper) context.removeAttribute("_OLD_FILTERS");
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
