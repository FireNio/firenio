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
package com.generallycloud.baseio.component;

import com.generallycloud.baseio.concurrent.AbstractEventLoopGroup;

/**
 * @author wangkai
 *
 */
public class SelectorEventLoopGroup extends AbstractEventLoopGroup {

    private SelectorEventLoop[]     selectorEventLoops;

    private NioSocketChannelContext channelContext;

    public SelectorEventLoopGroup(NioSocketChannelContext context, String eventLoopName,
            int eventLoopSize) {
        super(eventLoopName, eventLoopSize);
        this.channelContext = context;
    }

    @Override
    public SelectorEventLoop getNext() {
        return selectorEventLoops[getNextEventLoopIndex()];
    }

    @Override
    protected SelectorEventLoop[] initEventLoops() {
        selectorEventLoops = new SelectorEventLoop[getEventLoopSize()];
        return selectorEventLoops;
    }

    @Override
    public SelectorEventLoop[] getEventLoops() {
        return selectorEventLoops;
    }

    @Override
    protected SelectorEventLoop newEventLoop(int index) {
        return new SelectorEventLoop(this, index);
    }

    public NioSocketChannelContext getChannelContext() {
        return channelContext;
    }

}
