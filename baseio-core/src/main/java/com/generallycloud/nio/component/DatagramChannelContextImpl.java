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
package com.generallycloud.nio.component;

import java.math.BigDecimal;

import com.generallycloud.nio.Linkable;
import com.generallycloud.nio.buffer.PooledByteBufAllocatorManager;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.component.DatagramSessionManager.DatagramSessionManagerEvent;
import com.generallycloud.nio.configuration.ServerConfiguration;

public class DatagramChannelContextImpl extends AbstractChannelContext implements DatagramChannelContext {

	private DatagramPacketAcceptor				datagramPacketAcceptor;
	private DatagramSessionManager				sessionManager;
	private Linkable<DatagramSessionEventListener>	lastSessionEventListener;
	private Linkable<DatagramSessionEventListener>	sessionEventListenerLink;
	private Logger						logger		= LoggerFactory.getLogger(DatagramChannelContextImpl.class);

	public DatagramChannelContextImpl(ServerConfiguration configuration) {
		super(configuration);
	}
	
	@Override
	public void addSessionEventListener(DatagramSessionEventListener listener) {
		if (this.sessionEventListenerLink == null) {
			this.sessionEventListenerLink = new DatagramSEListenerWrapper(listener);
			this.lastSessionEventListener = this.sessionEventListenerLink;
		} else {
			this.lastSessionEventListener.setNext(new DatagramSEListenerWrapper(listener));
			this.lastSessionEventListener = this.lastSessionEventListener.getNext();
		}
	}

	@Override
	public Linkable<DatagramSessionEventListener> getSessionEventListenerLink() {
		return sessionEventListenerLink;
	}
	
	@Override
	protected void doStart() throws Exception {
		
		this.clearContext();

		this.serverConfiguration.initializeDefault(this);

		int SERVER_CORE_SIZE = serverConfiguration.getSERVER_CORE_SIZE();
		int server_port = serverConfiguration.getSERVER_PORT();
		long session_idle = serverConfiguration.getSERVER_SESSION_IDLE_TIME();

		long SERVER_MEMORY_POOL_CAPACITY = serverConfiguration.getSERVER_MEMORY_POOL_CAPACITY() * SERVER_CORE_SIZE;
		long SERVER_MEMORY_POOL_UNIT = serverConfiguration.getSERVER_MEMORY_POOL_UNIT();

		double MEMORY_POOL_SIZE = new BigDecimal(SERVER_MEMORY_POOL_CAPACITY * SERVER_MEMORY_POOL_UNIT)
				.divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP).doubleValue();

		this.encoding = serverConfiguration.getSERVER_ENCODING();
		this.sessionIdleTime = serverConfiguration.getSERVER_SESSION_IDLE_TIME();

		if (sessionManager == null) {
			this.sessionManager = new DatagramSessionManagerImpl(this);
		}
		
		if (getByteBufAllocatorManager() == null) {

			this.byteBufAllocatorManager = new PooledByteBufAllocatorManager(this);
			
			this.addSessionEventListener(new DatagramSessionManagerSEListener());
		}

		LoggerUtil.prettyNIOServerLog(logger,
				"======================================= service begin to start =======================================");
		LoggerUtil.prettyNIOServerLog(logger, "encoding              ：{ {} }", encoding);
		LoggerUtil.prettyNIOServerLog(logger, "cpu size              ：{ cpu * {} }", SERVER_CORE_SIZE);
		LoggerUtil.prettyNIOServerLog(logger, "session idle          ：{ {} }",session_idle);
		LoggerUtil.prettyNIOServerLog(logger, "listen port(udp)      ：{ {} }",server_port);
		LoggerUtil.prettyNIOServerLog(logger, "memory pool cap       ：{ {} * {} ≈ {} M }",
				new Object[] { SERVER_MEMORY_POOL_UNIT, SERVER_MEMORY_POOL_CAPACITY,
						MEMORY_POOL_SIZE });

		LifeCycleUtil.start(byteBufAllocatorManager);
	}
	
	@Override
	public void setSessionManager(DatagramSessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	@Override
	public DatagramSessionManager getSessionManager() {
		return sessionManager;
	}

	@Override
	public void offerSessionMEvent(DatagramSessionManagerEvent event) {
		sessionManager.offerSessionMEvent(event);
	}

	@Override
	protected void doStop() throws Exception {

		LifeCycleUtil.stop(sessionManager);

		LifeCycleUtil.stop(byteBufAllocatorManager);
	}

	@Override
	public DatagramPacketAcceptor getDatagramPacketAcceptor() {
		return datagramPacketAcceptor;
	}

	@Override
	public void setDatagramPacketAcceptor(DatagramPacketAcceptor datagramPacketAcceptor) {
		this.datagramPacketAcceptor = datagramPacketAcceptor;
	}

}
