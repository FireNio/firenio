package com.gifisan.nio.server.service;

import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.component.HotDeploy;
import com.gifisan.nio.component.PluginContext;

public interface PluginLoader extends HotDeploy, LifeCycle {

	public abstract PluginContext [] getPluginContexts();

}