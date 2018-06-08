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

public abstract class AbstractEventLoopGroup extends AbstractLifeCycle implements EventLoopGroup {

    private String             eventLoopName;
    private int                eventLoopSize;
    private FixedAtomicInteger eventLoopIndex;
    private EventLoopListener  eventLoopListener;

    protected AbstractEventLoopGroup() {}

    protected AbstractEventLoopGroup(String eventLoopName) {
        this.eventLoopName = eventLoopName;
    }

    protected AbstractEventLoopGroup(String eventLoopName, int eventLoopSize) {
        this.eventLoopName = eventLoopName;
        this.eventLoopSize = eventLoopSize;
    }

    @Override
    protected void doStart() throws Exception {
        this.eventLoopIndex = new FixedAtomicInteger(0, eventLoopSize - 1);
        EventLoop[] eventLoopArray = initEventLoops();
        for (int i = 0; i < eventLoopArray.length; i++) {
            eventLoopArray[i] = newEventLoop(i);
        }
        for (int i = 0; i < eventLoopArray.length; i++) {
            eventLoopArray[i].startup(eventLoopName + "-" + i);
        }
    }

    protected abstract EventLoop[] initEventLoops();

    protected int getNextEventLoopIndex() {
        return eventLoopIndex.getAndIncrement();
    }

    public int getEventLoopSize() {
        return eventLoopSize;
    }

    public void setEventLoopSize(int eventLoopSize) {
        this.eventLoopSize = eventLoopSize;
    }

    public String getEventLoopName() {
        return eventLoopName;
    }

    public void setEventLoopName(String eventLoopName) {
        this.eventLoopName = eventLoopName;
    }

    @Override
    public EventLoopListener getEventLoopListener() {
        return eventLoopListener;
    }

    @Override
    public void setEventLoopListener(EventLoopListener listener) {
        this.eventLoopListener = listener;
    }

    protected abstract EventLoop newEventLoop(int index);

    @Override
    protected void doStop() throws Exception {
        for (int i = 0; i < getEventLoopSize(); i++) {
            LifeCycleUtil.stop(getEventLoop(i));
        }
    }
}
