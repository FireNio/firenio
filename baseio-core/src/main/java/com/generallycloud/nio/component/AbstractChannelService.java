package com.generallycloud.nio.component;

import java.nio.channels.SelectableChannel;

public abstract class AbstractChannelService implements ChannelService {

	protected BaseContext		context;
	protected SelectableChannel	selectableChannel;
	
	public AbstractChannelService(BaseContext context) {
		this.context = context;
	}

	public BaseContext getContext() {
		return context;
	}

	public SelectableChannel getSelectableChannel() {
		return selectableChannel;
	}

}
