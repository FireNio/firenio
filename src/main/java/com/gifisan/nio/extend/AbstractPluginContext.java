package com.gifisan.nio.extend;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.gifisan.nio.extend.configuration.Configuration;
import com.gifisan.nio.extend.service.FutureAcceptorFilter;
import com.gifisan.nio.extend.service.FutureAcceptorService;

public abstract class AbstractPluginContext extends InitializeableImpl implements PluginContext {

	private int				pluginIndex	;
	private static AtomicInteger	_index		= new AtomicInteger();

	protected AbstractPluginContext() {
		this.pluginIndex = _index.getAndIncrement();
	}

	public int getPluginIndex() {
		return pluginIndex;
	}
	
	public void configFutureAcceptorFilter(List<FutureAcceptorFilter> filters) {
		
	}

	public void configFutureAcceptor(Map<String, FutureAcceptorService> acceptors) {
		
	}

	// FIXME you wen ti
	public void prepare(ApplicationContext context, Configuration config) throws Exception {
		this.initialize(context, config);
	}

	// FIXME you wen ti
	public void unload(ApplicationContext context, Configuration config) throws Exception {
		this.destroy(context, config);
	}

}
