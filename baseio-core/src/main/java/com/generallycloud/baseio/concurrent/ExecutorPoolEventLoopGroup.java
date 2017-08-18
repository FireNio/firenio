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

    private String            eventLoopName;

    private int               maxEventLoopSize;

    private int               maxEventQueueSize;

    private int               eventLoopSize;

    private ExecutorEventLoop eventLoop;

    private long              keepAliveTime;

    public ExecutorPoolEventLoopGroup(String eventLoopName, int maxEventLoopSize,
            int maxEventQueueSize, int eventLoopSize, long keepAliveTime) {
        this.eventLoopName = eventLoopName;
        this.maxEventLoopSize = maxEventLoopSize;
        this.maxEventQueueSize = maxEventQueueSize;
        this.eventLoopSize = eventLoopSize;
        this.keepAliveTime = keepAliveTime;
    }

    @Override
    public ExecutorEventLoop getNext() {
        return eventLoop;
    }

    @Override
    protected void doStart() throws Exception {

        eventLoop = new ExecutorPoolEventLoop(this, eventLoopSize, maxEventLoopSize,
                maxEventQueueSize, keepAliveTime);

        eventLoop.startup(eventLoopName);
    }

    @Override
    protected void doStop() throws Exception {
        LifeCycleUtil.stop(eventLoop);
    }

}
