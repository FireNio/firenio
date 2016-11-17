package com.generallycloud.nio.component;

import java.nio.channels.SelectableChannel;

public abstract class AbstractChannelService implements ChannelService {

	protected BaseContext		context;
	protected SelectableChannel	selectableChannel;

	public BaseContext getContext() {
		return context;
	}

	public void setContext(BaseContext context) {
		this.context = context;
	}

	public SelectableChannel getSelectableChannel() {
		return selectableChannel;
	}

}
