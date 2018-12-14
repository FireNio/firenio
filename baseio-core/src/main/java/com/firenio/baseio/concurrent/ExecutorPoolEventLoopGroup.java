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

import com.firenio.baseio.AbstractLifeCycle;
import com.firenio.baseio.LifeCycleUtil;
import com.firenio.baseio.common.Util;

public class ExecutorPoolEventLoopGroup extends AbstractLifeCycle
        implements ExecutorEventLoopGroup {

    private int               eventLoopSize;
    private int               maxQueueSize;
    private ExecutorEventLoop eventLoop;
    private EventLoopListener eventLoopListener;
    private String            eventLoopName;

    public ExecutorPoolEventLoopGroup() {
        this("event-process");
    }

    public ExecutorPoolEventLoopGroup(String eventLoopName) {
        this(eventLoopName, 1024 * 4 * Util.availableProcessors());
    }

    public ExecutorPoolEventLoopGroup(String eventLoopName, int maxQueueSize) {
        this(eventLoopName, Util.availableProcessors() * 2, maxQueueSize);
    }

    public ExecutorPoolEventLoopGroup(String name, int eventLoopSize, int maxQueueSize) {
        this.eventLoopName = name;
        this.maxQueueSize = maxQueueSize;
        this.eventLoopSize = eventLoopSize;
        this.eventLoop = new ExecutorPoolEventLoop(this);
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
    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    @Override
    public int getEventLoopSize() {
        return eventLoopSize;
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

}
