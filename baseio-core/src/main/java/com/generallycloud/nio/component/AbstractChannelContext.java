package com.generallycloud.nio.component;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.generallycloud.nio.AbstractLifeCycle;
import com.generallycloud.nio.buffer.MCByteBufAllocator;
import com.generallycloud.nio.configuration.ServerConfiguration;

public abstract class AbstractChannelContext extends AbstractLifeCycle implements ChannelContext {

	protected Charset							encoding;
	protected ServerConfiguration				serverConfiguration;
	protected long							sessionIdleTime;
	protected MCByteBufAllocator					mcByteBufAllocator;
	protected ChannelService					channelService;
	protected Map<Object, Object>				attributes	= new HashMap<Object, Object>();
	protected long							startupTime	;
	protected Sequence							sequence		;

	public MCByteBufAllocator getMcByteBufAllocator() {
		return mcByteBufAllocator;
	}
	
	protected void clearContext(){
		this.clearAttributes();
		this.startupTime = System.currentTimeMillis();
		this.sequence = new Sequence();
	}

	public AbstractChannelContext(ServerConfiguration configuration) {

		if (configuration == null) {
			throw new IllegalArgumentException("null configuration");
		}

		this.serverConfiguration = configuration;

		this.addLifeCycleListener(new ChannelContextListener());
	}

	public void clearAttributes() {
		this.attributes.clear();
	}

	public Object getAttribute(Object key) {
		return this.attributes.get(key);
	}

	public Set<Object> getAttributeNames() {
		return this.attributes.keySet();
	}

	public Charset getEncoding() {
		return encoding;
	}

	public ServerConfiguration getServerConfiguration() {
		return serverConfiguration;
	}

	public Object removeAttribute(Object key) {
		return this.attributes.remove(key);
	}

	public void setAttribute(Object key, Object value) {
		this.attributes.put(key, value);
	}

	public Sequence getSequence() {
		return sequence;
	}

	public long getSessionIdleTime() {
		return sessionIdleTime;
	}

	public long getStartupTime() {
		return startupTime;
	}

	public ChannelService getChannelService() {
		return channelService;
	}

	public void setChannelService(ChannelService channelService) {
		this.channelService = channelService;
	}

}
