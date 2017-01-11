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

import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.component.concurrent.ExecutorEventLoop;
import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

/**
 * @author wangkai
 *
 */
public interface SocketSelectorEventLoop extends SelectorEventLoop {

	public void accept(SocketChannel channel);

	@Override
	public abstract SocketChannelContext getChannelContext();

	public abstract ProtocolDecoder getProtocolDecoder();
	
	public abstract SocketSelector getSelector();

	public abstract ProtocolEncoder getProtocolEncoder();

	public abstract ProtocolFactory getProtocolFactory();

	public abstract ExecutorEventLoop getExecutorEventLoop();

	public abstract boolean isWaitForRegist();

	public abstract void setWaitForRegist(boolean isWaitForRegist);

	public abstract ReentrantLock getIsWaitForRegistLock();
	
	@Override
	public abstract SocketSelectorEventLoopGroup getEventLoopGroup();

}
