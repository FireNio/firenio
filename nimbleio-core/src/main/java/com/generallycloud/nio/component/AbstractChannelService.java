package com.generallycloud.nio.component;

import com.generallycloud.nio.configuration.ServerConfiguration;


public abstract class AbstractChannelService implements ChannelService {

	protected NIOContext	context	;

	public NIOContext getContext() {
		return context;
	}

	public void setContext(NIOContext context) {
		this.context = context;
	}

	protected abstract int getSERVER_PORT(ServerConfiguration configuration);
	
	protected abstract void setChannelService(NIOContext context);
	
	protected String getSERVER_HOST(ServerConfiguration configuration){
		return configuration.getSERVER_HOST();
	}
}
