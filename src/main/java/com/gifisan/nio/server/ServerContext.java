package com.gifisan.nio.server;

import java.nio.charset.Charset;

import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.component.FilterService;
import com.gifisan.nio.concurrent.ExecutorThreadPool;

public interface ServerContext extends Attributes, LifeCycle {

	public abstract String getAppLocalAddress();

	public abstract Charset getEncoding();

	public abstract ExecutorThreadPool getExecutorThreadPool();

	public abstract FilterService getFilterService();

	public abstract NIOServer getServer();

	public abstract ServerEndpointFactory getServerEndpointFactory();

	public abstract boolean redeploy();

	public abstract void setEncoding(Charset encoding);

}