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

import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.baseio.component.AbstractSocketSessionManager.SocketSessionManagerEvent;

/**
 * @author wangkai
 *
 */
public class NioGlobalSocketSessionManager implements SocketSessionManager {
	
	private SocketSessionManager [] socketSessionManagers;
	
	private int managerLen;
	
	public void init(NioSocketChannelContext context){
		NioChannelService service = (NioChannelService) context.getChannelService();
		SocketSelectorEventLoopGroup group = service.getSelectorEventLoopGroup();
		SocketSelectorEventLoop[] loops = group.getSelectorEventLoops();
		socketSessionManagers = new SocketSessionManager[loops.length];
		managerLen = loops.length;
		for (int i = 0; i < managerLen; i++) {
			socketSessionManagers[i] = loops[i].getSocketSessionManager();
		}
	}

	private AtomicInteger managedSessionSize = new AtomicInteger();
	
	@Override
	public int getManagedSessionSize(){
		return managedSessionSize.get();
	}

	@Override
	public SocketSession getSession(int sessionId){
		return socketSessionManagers[sessionId % managerLen].getSession(sessionId);
	}

	@Override
	public void offerSessionMEvent(SocketSessionManagerEvent event){
		for(SocketSessionManager m : socketSessionManagers){
			m.offerSessionMEvent(event);
		}
	}

	@Override
	public void loop() {
		
	}

	@Override
	public void stop() {
		
	}

	@Override
	public void putSession(SocketSession session) {
		managedSessionSize.incrementAndGet();
	}

	@Override
	public void removeSession(SocketSession session) {
		managedSessionSize.decrementAndGet();
	}
	

}
