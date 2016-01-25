package com.gifisan.mtp.server;

import java.nio.charset.Charset;

import com.gifisan.mtp.LifeCycle;
import com.gifisan.mtp.component.FilterService;
import com.gifisan.mtp.concurrent.ExecutorThreadPool;

public interface ServerContext extends Attributes, LifeCycle {

	public abstract String getAppLocalAddress();

	public abstract Charset getEncoding();

	public abstract ExecutorThreadPool getExecutorThreadPool();

	public abstract FilterService getFilterService();

	public abstract MTPServer getServer();

	public abstract ServerEndpointFactory getServerEndpointFactory();

	public abstract boolean redeploy();

	public abstract void setEncoding(Charset encoding);

}