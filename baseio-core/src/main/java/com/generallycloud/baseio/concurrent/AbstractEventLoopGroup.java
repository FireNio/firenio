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

    public AbstractEventLoopGroup(String eventLoopName, int eventLoopSize) {
        this.eventLoopName = eventLoopName;
        this.eventLoopSize = eventLoopSize;
        this.eventLoopIndex = new FixedAtomicInteger(0, eventLoopSize - 1);
    }

    @Override
    protected void doStart() throws Exception {

        EventLoop[] eventLoopArray = initEventLoops();

        for (int i = 0; i < eventLoopArray.length; i++) {

            eventLoopArray[i] = newEventLoop(i);
        }

        for (int i = 0; i < eventLoopArray.length; i++) {

            eventLoopArray[i].startup(eventLoopName + "-" + i);
        }
    }

    protected abstract EventLoop[] initEventLoops();

    protected abstract EventLoop[] getEventLoops();

    protected int getNextEventLoopIndex() {
        return eventLoopIndex.getAndIncrement();
    }

    protected int getEventLoopSize() {
        return eventLoopSize;
    }

    protected abstract EventLoop newEventLoop(int coreIndex);

    @Override
    protected void doStop() throws Exception {

        EventLoop[] eventLoopArray = getEventLoops();

        for (EventLoop el : eventLoopArray) {
            LifeCycleUtil.stop(el);
        }
    }
}
