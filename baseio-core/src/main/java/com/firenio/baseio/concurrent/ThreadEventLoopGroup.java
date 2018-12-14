/*
 * Copyright 2015 The Baseio Project
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
package com.firenio.baseio.concurrent;

import com.firenio.baseio.common.Util;

public class ThreadEventLoopGroup extends AbstractEventLoopGroup implements ExecutorEventLoopGroup {

    private int maxQueueSize;

    public ThreadEventLoopGroup() {
        this("event-process");
    }

    public ThreadEventLoopGroup(String eventLoopName) {
        this(eventLoopName, 1024 * 4);
    }

    public ThreadEventLoopGroup(String eventLoopName, int maxQueueSize) {
        this(eventLoopName, Util.availableProcessors() * 2, maxQueueSize);
    }

    public ThreadEventLoopGroup(String eventLoopName, int eventLoopSize, int maxQueueSize) {
        super(eventLoopName, eventLoopSize);
        this.maxQueueSize = maxQueueSize;
    }

    @Override
    protected ExecutorEventLoop newEventLoop(int coreIndex) {
        return new ThreadEventLoop(this);
    }

    @Override
    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    private ExecutorEventLoop[] executorEventLoops;

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
    public EventLoop getEventLoop(int index) {
        return executorEventLoops[index];
    }

}
