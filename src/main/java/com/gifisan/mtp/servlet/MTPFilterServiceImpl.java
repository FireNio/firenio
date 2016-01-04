package com.gifisan.mtp.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.Encoding;
import com.gifisan.mtp.LifeCycle;
import com.gifisan.mtp.common.FileUtil;
import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.component.FilterConfig;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.ServletContext;

public final class MTPFilterServiceImpl extends AbstractLifeCycle implements MTPFilterService, LifeCycle {

	private ServletContext	context		= null;

	private WrapperMTPFilter	rootFilter	= null;

	public MTPFilterServiceImpl(ServletContext context) {
		this.context = context;
	}

	public boolean doFilter(Request request, Response response) throws Exception {
		if (rootFilter == null) {
			return false;
		}
		WrapperMTPFilter filter = rootFilter;
		for (; filter != null;) {
			if (filter.doFilter(request, response)) {
				return true;
			}
			filter = filter.nextFilter();
		}
		return false;
	}

	private void loadFilters() {
		try {
			String str = FileUtil.readContentByCls("filters.config", Encoding.DEFAULT);
			if (StringUtil.isNullOrBlank(str)) {

				return;
			}

			JSONArray array = JSONArray.parseArray(str);

			if (array.size() > 0) {
				loadFilters0(array);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void loadFilters0(JSONArray array) {

		WrapperMTPFilter last = null;

		for (int i = 0; i < array.size(); i++) {
			JSONObject jObj = array.getJSONObject(i);
			String clazz = jObj.getString("class");
			Map<String, Object> config = toMap(jObj);
			FilterConfig filterConfig = new FilterConfig();
			filterConfig.setConfig(config);
			try {
				MTPFilter filter = (MTPFilter) Class.forName(clazz).newInstance();

				WrapperMTPFilterImpl _filter = new WrapperMTPFilterImpl(context, filter, filterConfig);

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
		// start all filter
		WrapperMTPFilter filter = rootFilter;
		for (; filter != null;) {
			try {
				filter.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
			filter = filter.nextFilter();
		}

	}

	protected void doStop() throws Exception {
		WrapperMTPFilter filter = rootFilter;
		for (; filter != null;) {
			try {
				LifeCycleUtil.stop(filter);
			} catch (Exception e) {
				e.printStackTrace();
			}
			filter = filter.nextFilter();
		}
	}

}
