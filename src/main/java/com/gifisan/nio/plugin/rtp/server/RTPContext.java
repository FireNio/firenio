package com.gifisan.nio.plugin.rtp.server;

import com.gifisan.nio.component.PluginContext;

public interface RTPContext extends PluginContext{
	
	public abstract RTPRoomFactory getRTPRoomFactory();
}