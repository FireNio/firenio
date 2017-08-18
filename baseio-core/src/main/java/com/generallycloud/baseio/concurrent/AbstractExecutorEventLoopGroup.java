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

/**
 * @author wangkai
 *
 */
public abstract class AbstractExecutorEventLoopGroup extends AbstractEventLoopGroup
        implements ExecutorEventLoopGroup {

    private ExecutorEventLoop[] executorEventLoops;

    public AbstractExecutorEventLoopGroup(String eventLoopName, int eventLoopSize) {
        super(eventLoopName, eventLoopSize);
    }

    @Override
    public ExecutorEventLoop getNext() {
        return executorEventLoops[getNextEventLoopIndex()];
    }

    @Override
    protected EventLoop[] initEventLoops() {
        executorEventLoops = new ExecutorEventLoop[getEventLoopSize()];
        return executorEventLoops;
    }

    @Override
    protected EventLoop[] getEventLoops() {
        return executorEventLoops;
    }
}
