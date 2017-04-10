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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.concurrent.Waiter;
import com.generallycloud.baseio.configuration.ServerConfiguration;

public abstract class AbstractChannelService implements ChannelService {

	protected boolean				active				= false;
	protected ReentrantLock			activeLock			= new ReentrantLock();
	protected InetSocketAddress		serverAddress			= null;
	protected Waiter<IOException>	shutDownWaiter			= new Waiter<>();

	@Override
	public InetSocketAddress getServerSocketAddress() {
		return serverAddress;
	}
	
	protected void initialize() throws IOException {

		ReentrantLock lock = this.activeLock;

		lock.lock();

		try {

			if (isActive()) {
				return;
			}

			if (getContext() == null) {
				throw new IllegalArgumentException("null nio context");
			}
			
			ChannelContext context = getContext();
			
			context.setChannelService(this);
			
			setServerCoreSize(context.getServerConfiguration());

			LifeCycleUtil.start(getContext());

			this.initService(context.getServerConfiguration());

			this.active = true;

		} finally {

			lock.unlock();
		}
	}
	
	protected abstract void initService(ServerConfiguration configuration) throws IOException;
	
	protected abstract void destroyService();

	protected Waiter<IOException> destroy(){
		
		ReentrantLock lock = this.activeLock;

		lock.lock();

		try {
			// just close
			this.destroyService();

			LifeCycleUtil.stop(getContext());

			shutDownWaiter.setPayload(null);

		} finally {

			active = false;

			lock.unlock();
		}
		
		return shutDownWaiter;
	}
	
	protected abstract void setServerCoreSize(ServerConfiguration configuration);

}
