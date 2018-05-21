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

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.spi.AsynchronousChannelProvider;

import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.concurrent.AioLineEventLoopGroup;
import com.generallycloud.baseio.concurrent.ExecutorEventLoopGroup;
import com.generallycloud.baseio.concurrent.ThreadEventLoopGroup;
import com.generallycloud.baseio.configuration.Configuration;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class AioSocketChannelContext extends AbstractSocketChannelContext {

    private AsynchronousChannelGroup         asynchronousChannelGroup;
    private ReadCompletionHandler            readCompletionHandler;
    private WriteCompletionHandler           writeCompletionHandler;
    private AioSessionManangerEventLoopGroup smEventLoopGroup;
    private AioSocketSessionManager          sessionManager;
    private ChannelService                   channelService;
    private Logger                           logger = LoggerFactory.getLogger(getClass());

    public AioSocketChannelContext(Configuration configuration) {
        super(configuration);
    }

    @Override
    protected ExecutorEventLoopGroup createExecutorEventLoopGroup() {
        int eventLoopSize = getConfiguration().getCoreSize();
        if (getConfiguration().isEnableWorkEventLoop()) {
            return new ThreadEventLoopGroup(this, "event-process", eventLoopSize);
        } else {
            return new AioLineEventLoopGroup("event-process", eventLoopSize);
        }
    }

    @Override
    protected void doStartModule() throws Exception {
        sessionManager = new AioSocketSessionManager(this);
        smEventLoopGroup = new AioSessionManangerEventLoopGroup("session-manager", 1, this);
        Configuration sc = getConfiguration();
        LifeCycleUtil.start(smEventLoopGroup);
        String threadName = "aio-process(tcp-" + sc.getPort() + ")";
        AsynchronousChannelProvider provider = AsynchronousChannelProvider.provider();
        CachedAioThreadFactory cachedAioThreadFactory = new CachedAioThreadFactory(this,
                threadName);
        this.asynchronousChannelGroup = provider
                .openAsynchronousChannelGroup(sc.getCoreSize(), cachedAioThreadFactory);
        super.doStartModule();
    }

    @Override
    protected void doStopModule() {
        try {
            this.asynchronousChannelGroup.shutdownNow();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        LifeCycleUtil.stop(smEventLoopGroup);
        super.doStopModule();
    }

    public AsynchronousChannelGroup getAsynchronousChannelGroup() {
        return asynchronousChannelGroup;
    }

    public ReadCompletionHandler getReadCompletionHandler() {
        return readCompletionHandler;
    }

    public WriteCompletionHandler getWriteCompletionHandler() {
        return writeCompletionHandler;
    }

    @Override
    public AioSocketSessionManager getSessionManager() {
        return sessionManager;
    }

    @Override
    public ChannelService getChannelService() {
        return channelService;
    }

    @Override
    public void setChannelService(ChannelService service) {
        this.channelService = service;
    }

}
