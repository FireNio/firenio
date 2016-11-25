package com.generallycloud.nio.component;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.generallycloud.nio.AbstractLifeCycle;
import com.generallycloud.nio.Linkable;
import com.generallycloud.nio.buffer.MCByteBufAllocator;
import com.generallycloud.nio.configuration.ServerConfiguration;

public abstract class AbstractChannelContext extends AbstractLifeCycle implements ChannelContext {

	protected Charset							encoding;
	protected Linkable<SessionEventListener>		lastSessionEventListener;
	protected ServerConfiguration				serverConfiguration;
	protected Linkable<SessionEventListener>		sessionEventListenerLink;
	protected SessionManager					sessionManager;
	protected long							sessionIdleTime;
	protected MCByteBufAllocator					mcByteBufAllocator;
	protected ChannelService					channelService;
	protected Map<Object, Object>				attributes	= new HashMap<Object, Object>();
	protected long							startupTime	= System.currentTimeMillis();
	protected Sequence							sequence		= new Sequence();

	public MCByteBufAllocator getMcByteBufAllocator() {
		return mcByteBufAllocator;
	}

	public AbstractChannelContext(ServerConfiguration configuration) {

		if (configuration == null) {
			throw new IllegalArgumentException("null configuration");
		}

		this.serverConfiguration = configuration;

		this.addLifeCycleListener(new BaseContextListener());
	}

	public void addSessionEventListener(SessionEventListener listener) {
		if (this.sessionEventListenerLink == null) {
			this.sessionEventListenerLink = new SessionEventListenerWrapper(listener);
			this.lastSessionEventListener = this.sessionEventListenerLink;
		} else {
			this.lastSessionEventListener.setNext(new SessionEventListenerWrapper(listener));
			this.lastSessionEventListener = this.lastSessionEventListener.getNext();
		}
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

	public Linkable<SessionEventListener> getSessionEventListenerLink() {
		return sessionEventListenerLink;
	}

	public SessionManager getSessionManager() {
		return sessionManager;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
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
