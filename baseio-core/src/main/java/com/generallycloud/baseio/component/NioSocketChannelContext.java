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

import java.nio.channels.SelectableChannel;

import com.generallycloud.baseio.concurrent.ExecutorEventLoopGroup;
import com.generallycloud.baseio.concurrent.FixedAtomicInteger;
import com.generallycloud.baseio.concurrent.LineEventLoopGroup;
import com.generallycloud.baseio.concurrent.ThreadEventLoopGroup;
import com.generallycloud.baseio.configuration.Configuration;

public class NioSocketChannelContext extends AbstractSocketChannelContext {

    private FixedAtomicInteger            channelIds;
    private ChannelService                channelService;
    private RichNioSocketSessionManager sessionManager;
    private SelectorEventLoopGroup        selectorEventLoopGroup;
    private SelectableChannel             selectableChannel;

    public NioSocketChannelContext(Configuration configuration) {
        super(configuration);
        this.sessionManager = new RichNioSocketSessionManager();
    }

    private FixedAtomicInteger createChannelIdsSequence() {
        int core_size = getConfiguration().getCoreSize();
        int max = (Integer.MAX_VALUE / core_size) * core_size - 1;
        return new FixedAtomicInteger(0, max);
    }

    @Override
    protected ExecutorEventLoopGroup createExecutorEventLoopGroup() {
        int eventLoopSize = getConfiguration().getCoreSize();
        if (getConfiguration().isEnableWorkEventLoop()) {
            return new ThreadEventLoopGroup(this, "event-process", eventLoopSize);
        } else {
            return new LineEventLoopGroup("event-process", eventLoopSize);
        }
    }

    @Override
    protected void doStartModule() throws Exception {
        channelIds = createChannelIdsSequence();
        super.doStartModule();
    }

    protected FixedAtomicInteger getChannelIds() {
        return channelIds;
    }

    @Override
    public ChannelService getChannelService() {
        return channelService;
    }

    @Override
    public RichNioSocketSessionManager getSessionManager() {
        return sessionManager;
    }

    @Override
    public void setChannelService(ChannelService service) {
        this.channelService = service;
    }

    public SelectorEventLoopGroup getSelectorEventLoopGroup() {
        return selectorEventLoopGroup;
    }

    public void setSelectorEventLoopGroup(SelectorEventLoopGroup selectorEventLoopGroup) {
        this.selectorEventLoopGroup = selectorEventLoopGroup;
    }

    public SelectableChannel getSelectableChannel() {
        return selectableChannel;
    }

    public void setSelectableChannel(SelectableChannel selectableChannel) {
        this.selectableChannel = selectableChannel;
    }

}
