package com.gifisan.mtp.component;

import com.gifisan.mtp.server.ServerContext;

public interface HotDeploy {

	public void onPreDeploy(ServerContext context, Configuration config) throws Exception;

	public void onSubDeploy(ServerContext context, Configuration config) throws Exception;

}
