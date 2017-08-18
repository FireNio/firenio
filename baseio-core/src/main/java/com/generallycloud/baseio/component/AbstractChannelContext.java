/*
 * Copyright 2015-2017 GenerallyCloud.com
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
package com.generallycloud.baseio.component;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.generallycloud.baseio.AbstractLifeCycle;
import com.generallycloud.baseio.buffer.ByteBufAllocatorManager;
import com.generallycloud.baseio.buffer.PooledByteBufAllocatorManager;
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocatorManager;
import com.generallycloud.baseio.configuration.ServerConfiguration;

public abstract class AbstractChannelContext extends AbstractLifeCycle implements ChannelContext {

    protected Charset                 encoding;
    protected ServerConfiguration     serverConfiguration;
    protected long                    sessionIdleTime;
    protected ChannelService          channelService;
    protected ByteBufAllocatorManager byteBufAllocatorManager;
    protected Map<Object, Object>     attributes  = new HashMap<>();
    protected long                    startupTime = System.currentTimeMillis();

    protected void clearContext() {
        this.clearAttributes();
    }

    @Override
    public ByteBufAllocatorManager getByteBufAllocatorManager() {
        return byteBufAllocatorManager;
    }

    public AbstractChannelContext(ServerConfiguration configuration) {

        if (configuration == null) {
            throw new IllegalArgumentException("null configuration");
        }

        this.serverConfiguration = configuration;

        this.addLifeCycleListener(new ChannelContextListener());

        this.sessionIdleTime = configuration.getSERVER_SESSION_IDLE_TIME();
    }

    protected void initializeByteBufAllocator() {

        if (getByteBufAllocatorManager() == null) {

            if (serverConfiguration.isSERVER_ENABLE_MEMORY_POOL()) {
                this.byteBufAllocatorManager = new PooledByteBufAllocatorManager(this);
            } else {
                this.byteBufAllocatorManager = new UnpooledByteBufAllocatorManager(this);
            }
        }
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

    @Override
    public void setByteBufAllocatorManager(ByteBufAllocatorManager byteBufAllocatorManager) {
        this.byteBufAllocatorManager = byteBufAllocatorManager;
    }

}
