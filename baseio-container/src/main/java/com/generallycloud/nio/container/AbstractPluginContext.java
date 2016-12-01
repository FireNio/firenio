package com.generallycloud.nio.container;

import java.util.List;
import java.util.Map;

import com.generallycloud.nio.container.configuration.Configuration;
import com.generallycloud.nio.container.service.FutureAcceptorFilter;
import com.generallycloud.nio.container.service.FutureAcceptorService;

public abstract class AbstractPluginContext extends InitializeableImpl implements PluginContext {

	private int	pluginIndex;

	protected AbstractPluginContext() {

		Sequence sequence = ApplicationContext.getInstance().getSequence();

		this.pluginIndex = sequence.AUTO_PLUGIN_INDEX.getAndIncrement();
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
