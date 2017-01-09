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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.component.concurrent.Waiter;
import com.generallycloud.nio.configuration.ServerConfiguration;

public abstract class AbstractChannelService implements ChannelService {

	protected InetSocketAddress		serverAddress		= null;
	protected boolean				active			= false;
	protected SelectableChannel		selectableChannel	= null;
	protected Waiter<IOException>	shutDownWaiter		= new Waiter<>();
	protected ReentrantLock			activeLock		= new ReentrantLock();
	protected SelectorEventLoopGroup	selectorEventLoopGroup;

	@Override
	public SelectableChannel getSelectableChannel() {
		return selectableChannel;
	}

	protected abstract void initselectableChannel() throws IOException;

	protected abstract SelectorEventLoop newSelectorLoop(SelectorEventLoop[] selectorLoops) throws IOException;

	protected void cancelService() {

		ReentrantLock lock = this.activeLock;

		lock.lock();

		try {
			// just close
			destroySelectorLoops();

			LifeCycleUtil.stop(getContext());

			shutDownWaiter.setPayload(null);

		} finally {

			active = false;

			lock.unlock();
		}
	}
	
	private void destroySelectorLoops() {
		LifeCycleUtil.stop(selectorEventLoopGroup);
	}

	protected void initSelectorLoops(SelectorEventLoopFactory factory){
		
		ServerConfiguration configuration = getContext().getServerConfiguration();

		int core_size = configuration.getSERVER_CORE_SIZE();
		//FIXME __limit eventQueueSize
		this.selectorEventLoopGroup = 
				new SelectorEventLoopGroupImpl("io-process", 0, core_size, factory);
		LifeCycleUtil.start(selectorEventLoopGroup);
	}

	protected void service() throws IOException {

		ReentrantLock lock = this.activeLock;

		lock.lock();

		try {

			if (active) {
				return;
			}

			if (getContext() == null) {
				throw new IllegalArgumentException("null nio context");
			}

			getContext().setChannelService(this);

			ServerConfiguration configuration = getContext().getServerConfiguration();

			if (!(this instanceof SocketChannelAcceptor)) {
				configuration.setSERVER_CORE_SIZE(1);
			}

			LifeCycleUtil.start(getContext());

			this.initselectableChannel();

			this.initService(configuration);

			this.active = true;

		} finally {

			lock.unlock();
		}
	}
	
	protected SelectorEventLoop [] getSelectorEventLoops(){
		return selectorEventLoopGroup.getSelectorEventLoops();
	}

	@Override
	public InetSocketAddress getServerSocketAddress() {
		return serverAddress;
	}

	protected abstract void initService(ServerConfiguration configuration) throws IOException;

}
