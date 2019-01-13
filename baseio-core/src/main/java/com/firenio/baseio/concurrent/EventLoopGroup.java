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

import com.firenio.baseio.LifeCycle;
import com.firenio.baseio.common.Util;

public abstract class EventLoopGroup extends LifeCycle {

    private FixedAtomicInteger eventLoopIndex;
    private String             eventLoopName;
    private int                eventLoopSize;
    private int                maxQueueSize;

    protected EventLoopGroup(String eventLoopName) {
        this(eventLoopName, Util.availableProcessors());
    }

    protected EventLoopGroup(String eventLoopName, int eventLoopSize) {
        this(eventLoopName, eventLoopSize, 1024 * 4);
    }

    protected EventLoopGroup(String eventLoopName, int eventLoopSize, int maxQueueSize) {
        if (eventLoopSize < 1) {
            eventLoopSize = 1;
        }
        if (maxQueueSize < 1) {
            maxQueueSize = 1024 * 8;
        }
        this.eventLoopName = eventLoopName;
        this.eventLoopSize = eventLoopSize;
        this.maxQueueSize = maxQueueSize;
    }

    @Override
    protected void doStart() throws Exception {
        this.eventLoopIndex = new FixedAtomicInteger(0, eventLoopSize - 1);
        EventLoop[] eventLoopArray = initEventLoops();
        if (eventLoopArray.length == 1) {
            eventLoopArray[0] = newEventLoop(0, getEventLoopName());
            Util.start(eventLoopArray[0]);
        } else {
            for (int i = 0; i < eventLoopArray.length; i++) {
                eventLoopArray[i] = newEventLoop(i, eventLoopName + "-" + i);
            }
            for (int i = 0; i < eventLoopArray.length; i++) {
                Util.start(eventLoopArray[i]);
            }
        }
    }

    @Override
    protected void doStop() {
        for (int i = 0; i < getEventLoopSize(); i++) {
            Util.stop(getEventLoop(i));
        }
    }

    public abstract EventLoop getEventLoop(int i);

    public String getEventLoopName() {
        return eventLoopName;
    }

    public int getEventLoopSize() {
        return eventLoopSize;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public abstract EventLoop getNext();

    protected int getNextEventLoopIndex() {
        return eventLoopIndex.getAndIncrement();
    }

    protected EventLoop[] initEventLoops() {
        return null;
    }

    protected EventLoop newEventLoop(int index, String threadName) throws Exception {
        return null;
    }

    public void setEventLoopSize(int eventLoopSize) {
        this.checkNotRunning();
        this.eventLoopSize = eventLoopSize;
    }

    public void setMaxQueueSize(int maxQueueSize) {
        this.checkNotRunning();
        this.maxQueueSize = maxQueueSize;
    }

}
