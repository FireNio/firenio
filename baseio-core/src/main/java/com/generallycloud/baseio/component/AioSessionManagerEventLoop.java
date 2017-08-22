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

import com.generallycloud.baseio.concurrent.ExecutorEventLoopGroup;
import com.generallycloud.baseio.concurrent.ThreadEventLoop;

/**
 * @author wangkai
 *
 */
public class AioSessionManagerEventLoop extends ThreadEventLoop {

    private SocketSessionManager sessionManager;

    public AioSessionManagerEventLoop(ExecutorEventLoopGroup eventLoopGroup,
            AioSocketChannelContext context) {
        super(eventLoopGroup, context);
        this.sessionManager = context.getSessionManager();
    }

    @Override
    protected void doLoop() throws InterruptedException {

        super.doLoop();

        sessionManager.loop();
    }

    /**
     * @return the sessionManager
     */
    public SocketSessionManager getSessionManager() {
        return sessionManager;
    }
}
