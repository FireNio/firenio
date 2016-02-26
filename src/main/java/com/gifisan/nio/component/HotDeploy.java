package com.gifisan.nio.component;

import com.gifisan.nio.server.ServerContext;

public interface HotDeploy {

	public void prepare(ServerContext context, Configuration config) throws Exception;

	public void unload(ServerContext context, Configuration config) throws Exception;

}
