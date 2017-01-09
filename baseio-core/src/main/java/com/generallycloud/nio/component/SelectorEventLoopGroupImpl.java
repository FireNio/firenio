/*
 * Copyright 2015 GenerallyCloud.com
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
package com.generallycloud.nio.component;

import com.generallycloud.nio.component.concurrent.AbstractEventLoopGroup;
import com.generallycloud.nio.component.concurrent.EventLoop;

/**
 * @author wangkai
 *
 */
public class SelectorEventLoopGroupImpl extends AbstractEventLoopGroup implements SelectorEventLoopGroup{
	
	public SelectorEventLoopGroupImpl(String eventLoopName, int eventQueueSize, int eventLoopSize) {
		super(eventLoopName, eventQueueSize, eventLoopSize);
	}
	
	protected SelectorEventLoop[]	selectorEventLoops		= null;
	
	public SelectorEventLoopGroupImpl(String eventLoopName, int eventQueueSize, int eventLoopSize,
			SelectorEventLoopFactory selectorEventLoopFactory) {
		super(eventLoopName, eventQueueSize, eventLoopSize);
		this.selectorEventLoopFactory = selectorEventLoopFactory;
	}

	private SelectorEventLoopFactory selectorEventLoopFactory = null;
	
	@Override
	public SelectorEventLoop getNext() {
		return selectorEventLoops[getNextEventLoopIndex()];
	}

	@Override
	public SelectorEventLoop[] getSelectorEventLoops() {
		return selectorEventLoops;
	}

	@Override
	protected EventLoop[] initEventLoops() {
		selectorEventLoops = new SelectorEventLoop[getEventLoopSize()];
		return selectorEventLoops;
	}

	@Override
	protected EventLoop[] getEventLoops() {
		return getSelectorEventLoops();
	}

	@Override
	protected SelectorEventLoop newEventLoop(int eventQueueSize) {
		return selectorEventLoopFactory.newEventLoop(this,eventQueueSize);
	}
	
}
