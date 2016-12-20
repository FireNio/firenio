/*
 * Copyright 2015 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
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

	@Override
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

	@Override
	public void clearAttributes() {
		this.attributes.clear();
	}

	@Override
	public Object getAttribute(Object key) {
		return this.attributes.get(key);
	}

	@Override
	public Set<Object> getAttributeNames() {
		return this.attributes.keySet();
	}

	@Override
	public Charset getEncoding() {
		return encoding;
	}

	@Override
	public ServerConfiguration getServerConfiguration() {
		return serverConfiguration;
	}

	@Override
	public Object removeAttribute(Object key) {
		return this.attributes.remove(key);
	}

	@Override
	public void setAttribute(Object key, Object value) {
		this.attributes.put(key, value);
	}

	@Override
	public Sequence getSequence() {
		return sequence;
	}

	@Override
	public long getSessionIdleTime() {
		return sessionIdleTime;
	}

	@Override
	public long getStartupTime() {
		return startupTime;
	}

	@Override
	public ChannelService getChannelService() {
		return channelService;
	}

	@Override
	public void setChannelService(ChannelService channelService) {
		this.channelService = channelService;
	}

}
