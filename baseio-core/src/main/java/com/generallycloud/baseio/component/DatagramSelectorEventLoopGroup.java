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

import com.generallycloud.baseio.component.concurrent.AbstractEventLoopGroup;
import com.generallycloud.baseio.component.concurrent.EventLoop;

/**
 * @author wangkai
 *
 */
public class DatagramSelectorEventLoopGroup extends AbstractEventLoopGroup implements SelectorEventLoopGroup {

	private DatagramSelectorEventLoop[]	selectorEventLoops	= null;

	private DatagramChannelContext		channelContext;

	public DatagramSelectorEventLoopGroup(DatagramChannelContext context, String eventLoopName, int eventQueueSize,
			int eventLoopSize) {
		super(eventLoopName, eventQueueSize, eventLoopSize);
		this.channelContext = context;
	}

	@Override
	public DatagramSelectorEventLoop getNext() {
		return selectorEventLoops[getNextEventLoopIndex()];
	}

	@Override
	public DatagramSelectorEventLoop[] getSelectorEventLoops() {
		return selectorEventLoops;
	}

	@Override
	protected EventLoop[] initEventLoops() {
		selectorEventLoops = new DatagramSelectorEventLoop[getEventLoopSize()];
		return selectorEventLoops;
	}

	@Override
	protected EventLoop[] getEventLoops() {
		return getSelectorEventLoops();
	}

	@Override
	protected DatagramSelectorEventLoop newEventLoop(int coreIndex, int eventQueueSize) {
		return new DatagramSelectorEventLoopImpl(this,coreIndex);
	}

	@Override
	public DatagramChannelContext getChannelContext() {
		return channelContext;
	}
}
