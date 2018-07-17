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
import com.generallycloud.baseio.common.Assert;
import com.generallycloud.baseio.component.RejectedExecutionHandle;
import com.generallycloud.baseio.component.RejectedExecutionHandle.DefaultRejectedExecutionHandle;

public class ExecutorPoolEventLoopGroup extends AbstractLifeCycle
        implements ExecutorEventLoopGroup {

    private ExecutorEventLoop eventLoop;
    private EventLoopListener eventLoopListener;
    private String            eventLoopName;
    private RejectedExecutionHandle rejectedExecutionHandle = new DefaultRejectedExecutionHandle();

    public ExecutorPoolEventLoopGroup(String eventLoopName, int coreEventLoopSize,
            int maxEventLoopSize, int maxEventQueueSize, long keepAliveTime) {
        this.eventLoopName = eventLoopName;
        this.eventLoop = new ExecutorPoolEventLoop(this, coreEventLoopSize, maxEventLoopSize,
                maxEventQueueSize, keepAliveTime);
    }

    @Override
    protected void doStart() throws Exception {
        if (eventLoopListener != null) {
            eventLoopListener.onStartup(eventLoop);
        }
        eventLoop.startup(eventLoopName);
    }

    @Override
    protected void doStop() throws Exception {
        if (eventLoopListener != null) {
            eventLoopListener.onStop(eventLoop);
        }
        LifeCycleUtil.stop(eventLoop);
    }

    @Override
    public EventLoop getEventLoop(int index) {
        return eventLoop;
    }

    @Override
    public EventLoopListener getEventLoopListener() {
        return eventLoopListener;
    }

    @Override
    public ExecutorEventLoop getNext() {
        return eventLoop;
    }

    @Override
    public void setEventLoopListener(EventLoopListener eventLoopListener) {
        this.eventLoopListener = eventLoopListener;
    }

    @Override
    public RejectedExecutionHandle getRejectedExecutionHandle() {
        return rejectedExecutionHandle;
    }

    public void setRejectedExecutionHandle(RejectedExecutionHandle rejectedExecutionHandle) {
        Assert.notNull(rejectedExecutionHandle, "null rejectedExecutionHandle");
        this.rejectedExecutionHandle = rejectedExecutionHandle;
    }

}
