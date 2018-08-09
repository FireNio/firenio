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

public abstract class AbstractEventLoopGroup extends AbstractLifeCycle implements EventLoopGroup {

    private FixedAtomicInteger      eventLoopIndex;
    private EventLoopListener       eventLoopListener;
    private String                  eventLoopName;
    private int                     eventLoopSize;
    private RejectedExecutionHandle rejectedExecutionHandle = new DefaultRejectedExecutionHandle();

    protected AbstractEventLoopGroup() {}

    protected AbstractEventLoopGroup(String eventLoopName) {
        this.eventLoopName = eventLoopName;
    }

    protected AbstractEventLoopGroup(String eventLoopName, int eventLoopSize) {
        if (eventLoopSize < 1) {
            eventLoopSize = 1;
        }
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

    @Override
    protected void doStop() throws Exception {
        for (int i = 0; i < getEventLoopSize(); i++) {
            LifeCycleUtil.stop(getEventLoop(i));
        }
    }

    @Override
    public EventLoopListener getEventLoopListener() {
        return eventLoopListener;
    }

    public String getEventLoopName() {
        return eventLoopName;
    }

    public int getEventLoopSize() {
        return eventLoopSize;
    }

    protected int getNextEventLoopIndex() {
        return eventLoopIndex.getAndIncrement();
    }

    public RejectedExecutionHandle getRejectedExecutionHandle() {
        return rejectedExecutionHandle;
    }

    protected abstract EventLoop[] initEventLoops();

    protected abstract EventLoop newEventLoop(int index);

    @Override
    public void setEventLoopListener(EventLoopListener listener) {
        this.eventLoopListener = listener;
    }

    public void setEventLoopName(String eventLoopName) {
        this.eventLoopName = eventLoopName;
    }

    public void setEventLoopSize(int eventLoopSize) {
        this.eventLoopSize = eventLoopSize;
    }

    public void setRejectedExecutionHandle(RejectedExecutionHandle rejectedExecutionHandle) {
        Assert.notNull(rejectedExecutionHandle, "null rejectedExecutionHandle");
        this.rejectedExecutionHandle = rejectedExecutionHandle;
    }

}
