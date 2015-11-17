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

public class ExecutorEventLoopGroup extends EventLoopGroup {

    private ExecutorEventLoop eventLoop;

    public ExecutorEventLoopGroup() {
        this("event-process");
    }

    public ExecutorEventLoopGroup(String eventLoopName) {
        this(eventLoopName, 1024 * 4 * Util.availableProcessors());
    }

    public ExecutorEventLoopGroup(String eventLoopName, int maxQueueSize) {
        this(eventLoopName, Util.availableProcessors() * 2, maxQueueSize);
    }

    public ExecutorEventLoopGroup(String name, int eventLoopSize, int maxQueueSize) {
        super(name, eventLoopSize, maxQueueSize);
    }

    @Override
    protected void doStart() throws Exception {
        if (eventLoop == null) {
            this.eventLoop = new ExecutorEventLoop(this);
        }
        Util.start(eventLoop);
    }

    @Override
    protected void doStop() {
        Util.stop(eventLoop);
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
