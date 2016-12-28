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

import java.math.BigDecimal;

import com.generallycloud.nio.Linkable;
import com.generallycloud.nio.buffer.MCByteBufAllocator;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.component.SocketSessionManager.SocketSessionManagerEvent;
import com.generallycloud.nio.component.concurrent.EventLoopGroup;
import com.generallycloud.nio.component.concurrent.LineEventLoopGroup;
import com.generallycloud.nio.component.concurrent.ThreadEventLoopGroup;
import com.generallycloud.nio.component.ssl.SslContext;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.protocol.EmptyReadFuture;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public class SocketChannelContextImpl extends AbstractChannelContext implements SocketChannelContext {

	private IoEventHandleAdaptor				ioEventHandleAdaptor;
	private ProtocolFactory					protocolFactory;
	private BeatFutureFactory				beatFutureFactory;
	private int							sessionAttachmentSize;
	private EventLoopGroup					eventLoopGroup;
	private ProtocolEncoder					protocolEncoder;
	private SslContext						sslContext;
	private boolean						enableSSL;
	private ChannelByteBufReader				channelByteBufReader;
	private ForeReadFutureAcceptor			foreReadFutureAcceptor;
	private SocketSessionManager				sessionManager;
	private Linkable<SocketSessionEventListener>	lastSessionEventListener;
	private Linkable<SocketSessionEventListener>	sessionEventListenerLink;
	private SocketSessionFactory				sessionFactory;
	private Logger							logger	= LoggerFactory.getLogger(SocketChannelContextImpl.class);

	@Override
	public int getSessionAttachmentSize() {
		return sessionAttachmentSize;
	}

	@Override
	public void addSessionEventListener(SocketSessionEventListener listener) {
		if (this.sessionEventListenerLink == null) {
			this.sessionEventListenerLink = new SocketSEListenerWrapper(listener);
			this.lastSessionEventListener = this.sessionEventListenerLink;
		} else {
			this.lastSessionEventListener.setNext(new SocketSEListenerWrapper(listener));
			this.lastSessionEventListener = this.lastSessionEventListener.getNext();
		}
	}

	@Override
	public void offerSessionMEvent(SocketSessionManagerEvent event) {
		sessionManager.offerSessionMEvent(event);
	}

	@Override
	public Linkable<SocketSessionEventListener> getSessionEventListenerLink() {
		return sessionEventListenerLink;
	}

	@Override
	public SocketSessionManager getSessionManager() {
		return sessionManager;
	}

	@Override
	public void setSessionAttachmentSize(int sessionAttachmentSize) {
		this.sessionAttachmentSize = sessionAttachmentSize;
	}

	@Override
	public BeatFutureFactory getBeatFutureFactory() {
		return beatFutureFactory;
	}

	@Override
	public void setBeatFutureFactory(BeatFutureFactory beatFutureFactory) {
		this.beatFutureFactory = beatFutureFactory;
	}

	@Override
	public ProtocolEncoder getProtocolEncoder() {
		return protocolEncoder;
	}

	public SocketChannelContextImpl(ServerConfiguration configuration) {
		super(configuration);
	}
	
	@Override
	protected void doStart() throws Exception {

		if (ioEventHandleAdaptor == null) {
			throw new IllegalArgumentException("null ioEventHandle");
		}

		if (protocolFactory == null) {
			throw new IllegalArgumentException("null protocolFactory");
		}
		
		this.clearContext();

		this.serverConfiguration.initializeDefault(this);
		
		EmptyReadFuture.initializeReadFuture(this);
		
		if (isEnableSSL()) {
			this.sslContext.initialize(this);
		}

		int SERVER_CORE_SIZE = serverConfiguration.getSERVER_CORE_SIZE();

		long SERVER_MEMORY_POOL_CAPACITY = serverConfiguration.getSERVER_MEMORY_POOL_CAPACITY() * SERVER_CORE_SIZE;
		long SERVER_MEMORY_POOL_UNIT = serverConfiguration.getSERVER_MEMORY_POOL_UNIT();

		double MEMORY_POOL_SIZE = new BigDecimal(SERVER_MEMORY_POOL_CAPACITY * SERVER_MEMORY_POOL_UNIT)
				.divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP).doubleValue();

		this.encoding = serverConfiguration.getSERVER_ENCODING();
		this.sessionIdleTime = serverConfiguration.getSERVER_SESSION_IDLE_TIME();

		if (protocolEncoder == null) {
			this.protocolEncoder = protocolFactory.getProtocolEncoder();
		}

		if (mcByteBufAllocator == null) {

			this.mcByteBufAllocator = new MCByteBufAllocator(this);
			
			this.addSessionEventListener(new SocketSessionManagerSEListener());
		}

		LoggerUtil.prettyNIOServerLog(logger,
				"======================================= 服务开始启动 =======================================");
		LoggerUtil.prettyNIOServerLog(logger, "项目编码           ：{ {} }", encoding);
		LoggerUtil.prettyNIOServerLog(logger, "协议名称           ：{ {} }", protocolFactory.getProtocolID());
		LoggerUtil.prettyNIOServerLog(logger, "CPU核心数          ：{ CPU * {} }", SERVER_CORE_SIZE);
		LoggerUtil.prettyNIOServerLog(logger, "启用SSL加密        ：{ {} }", isEnableSSL());
		LoggerUtil.prettyNIOServerLog(logger, "SESSION_IDLE       ：{ {} }",
				serverConfiguration.getSERVER_SESSION_IDLE_TIME());
		LoggerUtil.prettyNIOServerLog(logger, "监听端口(TCP)      ：{ {} }", serverConfiguration.getSERVER_PORT());
		LoggerUtil.prettyNIOServerLog(logger, "内存池容量         ：{ {} * {} ≈ {} M }",
				new Object[] { SERVER_MEMORY_POOL_UNIT, SERVER_MEMORY_POOL_CAPACITY, MEMORY_POOL_SIZE });

		LifeCycleUtil.start(ioEventHandleAdaptor);
		
		if (eventLoopGroup == null) {
			
			int eventQueueSize = serverConfiguration.getSERVER_IO_EVENT_QUEUE();
			
			int eventLoopSize = serverConfiguration.getSERVER_CORE_SIZE();
			
			if (serverConfiguration.isSERVER_ENABLE_WORK_EVENT_LOOP()) {
				this.eventLoopGroup = new ThreadEventLoopGroup("event-process", eventQueueSize, eventLoopSize);
			} else {
				this.eventLoopGroup = new LineEventLoopGroup("event-process", eventQueueSize, eventLoopSize);
			}
		}
		
		if (foreReadFutureAcceptor == null) {
			this.foreReadFutureAcceptor = new EventLoopReadFutureAcceptor();
		}

		if (channelByteBufReader == null) {

			this.channelByteBufReader = new IoLimitChannelByteBufReader();
			
			if (enableSSL) {
				getLastChannelByteBufReader(channelByteBufReader).setNext(new SslChannelByteBufReader());
			}
			
			getLastChannelByteBufReader(channelByteBufReader).setNext(new TransparentByteBufReader(this));
		}

		if (sessionManager == null) {
			sessionManager = new SocketSessionManagerImpl(this);
		}

		if (sessionFactory == null) {
			sessionFactory = new SocketSessionFactoryImpl();
		}

		LifeCycleUtil.start(mcByteBufAllocator);

		LifeCycleUtil.start(eventLoopGroup);
	}

	private ChannelByteBufReader getLastChannelByteBufReader(ChannelByteBufReader value) {

		for (;;) {

			if (value.getNext() == null) {
				return value;
			}

			value = value.getNext().getValue();
		}
	}

	@Override
	protected void doStop() throws Exception {

		CloseUtil.close(sessionManager);

		LifeCycleUtil.stop(eventLoopGroup);

		LifeCycleUtil.stop(ioEventHandleAdaptor);

		LifeCycleUtil.stop(mcByteBufAllocator);
	}

	@Override
	public ProtocolFactory getProtocolFactory() {
		return protocolFactory;
	}

	@Override
	public IoEventHandleAdaptor getIoEventHandleAdaptor() {
		return ioEventHandleAdaptor;
	}

	@Override
	public EventLoopGroup getEventLoopGroup() {
		return eventLoopGroup;
	}

	@Override
	public void setIoEventHandleAdaptor(IoEventHandleAdaptor ioEventHandleAdaptor) {
		this.ioEventHandleAdaptor = ioEventHandleAdaptor;
	}

	@Override
	public void setProtocolFactory(ProtocolFactory protocolFactory) {
		this.protocolFactory = protocolFactory;
	}
	
	@Override
	public void setEventLoopGroup(EventLoopGroup eventLoopGroup) {
		this.eventLoopGroup = eventLoopGroup;
	}

	@Override
	public SslContext getSslContext() {
		return sslContext;
	}

	@Override
	public void setSslContext(SslContext sslContext) {
		if (sslContext == null) {
			throw new IllegalArgumentException("null sslContext");
		}
		this.sslContext = sslContext;
		this.enableSSL = true;
	}

	@Override
	public boolean isEnableSSL() {
		return enableSSL;
	}

	@Override
	public SocketSessionFactory getSessionFactory() {
		return sessionFactory;
	}

	@Override
	public void setSocketSessionFactory(SocketSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public ChannelByteBufReader getChannelByteBufReader() {
		return channelByteBufReader;
	}

	@Override
	public ForeReadFutureAcceptor getForeReadFutureAcceptor() {
		return foreReadFutureAcceptor;
	}

	@Override
	public void setSessionManager(SocketSessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

}
