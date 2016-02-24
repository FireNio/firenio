package com.gifisan.nio.component;

import com.gifisan.nio.server.ServerContext;

public interface HotDeploy {

	public void onPreDeploy(ServerContext context, Configuration config) throws Exception;

	public void onSubDeploy(ServerContext context, Configuration config) throws Exception;

}
