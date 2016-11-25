package com.generallycloud.nio.component;

import java.nio.charset.Charset;

import com.generallycloud.nio.Attributes;
import com.generallycloud.nio.LifeCycle;
import com.generallycloud.nio.buffer.MCByteBufAllocator;
import com.generallycloud.nio.configuration.ServerConfiguration;

public interface ChannelContext extends Attributes, LifeCycle {

	public abstract SessionManager getSessionManager();

	public abstract Charset getEncoding();

	public abstract ServerConfiguration getServerConfiguration();

	public abstract ChannelService getChannelService();

	public abstract void setChannelService(ChannelService service);

	public abstract Sequence getSequence();

	public abstract long getSessionIdleTime();

	public abstract long getStartupTime();

	public abstract MCByteBufAllocator getMcByteBufAllocator();
	
}