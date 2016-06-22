package com.gifisan.nio.component;

import com.gifisan.nio.server.NIOContext;

public interface HotDeploy {

	public void prepare(NIOContext context, Configuration config) throws Exception;

	public void unload(NIOContext context, Configuration config) throws Exception;

}
