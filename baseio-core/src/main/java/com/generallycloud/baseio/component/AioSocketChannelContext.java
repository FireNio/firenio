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
import com.generallycloud.baseio.configuration.ServerConfiguration;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class AioSocketChannelContext extends AbstractSocketChannelContext {

    private AsynchronousChannelGroup         asynchronousChannelGroup;

    private ReadCompletionHandler            readCompletionHandler;

    private WriteCompletionHandler           writeCompletionHandler;

    private AioSessionManangerEventLoopGroup sessionManangerEventLoopGroup;

    private AioSocketSessionManager          sessionManager;

    private Logger                           logger = LoggerFactory.getLogger(getClass());

    public AioSocketChannelContext(ServerConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected void doStartModule() throws Exception {

        sessionManangerEventLoopGroup = new AioSessionManangerEventLoopGroup("session-manager", 1,
                this);

        LifeCycleUtil.start(sessionManangerEventLoopGroup);

        sessionManager = new AioSocketSessionManager(this);

        initializeChannelGroup(serverConfiguration.getSERVER_CORE_SIZE());

        super.doStartModule();
    }

    @Override
    protected void doStopModule() {

        try {
            this.asynchronousChannelGroup.shutdownNow();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        LifeCycleUtil.stop(sessionManangerEventLoopGroup);

        super.doStopModule();
    }

    private void initializeChannelGroup(int SERVER_CORE_SIZE) throws IOException {

        AsynchronousChannelProvider provider = AsynchronousChannelProvider.provider();

        CachedAioThreadFactory cachedAioThreadFactory = new CachedAioThreadFactory(this, "tcp");

        this.asynchronousChannelGroup = provider.openAsynchronousChannelGroup(SERVER_CORE_SIZE,
                cachedAioThreadFactory);
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

}
