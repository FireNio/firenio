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
package com.generallycloud.baseio.concurrent;

import com.generallycloud.baseio.AbstractLifeCycle;
import com.generallycloud.baseio.LifeCycleUtil;

public class ExecutorPoolEventLoopGroup extends AbstractLifeCycle
        implements ExecutorEventLoopGroup {

    private ExecutorEventLoop eventLoop;
    private String            eventLoopName;

    public ExecutorPoolEventLoopGroup(String eventLoopName, int coreEventLoopSize,
            int maxEventLoopSize, int maxEventQueueSize, long keepAliveTime) {
        this.eventLoopName = eventLoopName;
        this.eventLoop = new ExecutorPoolEventLoop(this, coreEventLoopSize, maxEventLoopSize,
                maxEventQueueSize, keepAliveTime);
    }

    @Override
    protected void doStart() throws Exception {
        eventLoop.startup(eventLoopName);
    }

    @Override
    protected void doStop() throws Exception {
        LifeCycleUtil.stop(eventLoop);
    }

    @Override
    public EventLoop getEventLoop(int index) {
        return eventLoop;
    }

    @Override
    public ExecutorEventLoop getNext() {
        return eventLoop;
    }

}
