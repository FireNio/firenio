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

	public int getSessionAttachmentSize() {
		return sessionAttachmentSize;
	}

	public void addSessionEventListener(SocketSessionEventListener listener) {
		if (this.sessionEventListenerLink == null) {
			this.sessionEventListenerLink = new SocketSEListenerWrapper(listener);
			this.lastSessionEventListener = this.sessionEventListenerLink;
		} else {
			this.lastSessionEventListener.setNext(new SocketSEListenerWrapper(listener));
			this.lastSessionEventListener = this.lastSessionEventListener.getNext();
		}
	}

	public void offerSessionMEvent(SocketSessionManagerEvent event) {
		sessionManager.offerSessionMEvent(event);
	}

	public Linkable<SocketSessionEventListener> getSessionEventListenerLink() {
		return sessionEventListenerLink;
	}

	public SocketSessionManager getSessionManager() {
		return sessionManager;
	}

	public void setSessionAttachmentSize(int sessionAttachmentSize) {
		this.sessionAttachmentSize = sessionAttachmentSize;
	}

	public BeatFutureFactory getBeatFutureFactory() {
		return beatFutureFactory;
	}

	public void setBeatFutureFactory(BeatFutureFactory beatFutureFactory) {
		this.beatFutureFactory = beatFutureFactory;
	}

	public ProtocolEncoder getProtocolEncoder() {
		return protocolEncoder;
	}

	public SocketChannelContextImpl(ServerConfiguration configuration) {
		super(configuration);
	}

	protected void doStart() throws Exception {

		if (ioEventHandleAdaptor == null) {
			throw new IllegalArgumentException("null ioEventHandle");
		}

		if (protocolFactory == null) {
			throw new IllegalArgumentException("null protocolFactory");
		}

		serverConfiguration.initializeDefault(this);

		int SERVER_CORE_SIZE = serverConfiguration.getSERVER_CORE_SIZE();

		long SERVER_MEMORY_POOL_CAPACITY = serverConfiguration.getSERVER_MEMORY_POOL_CAPACITY() * SERVER_CORE_SIZE;
		long SERVER_MEMORY_POOL_UNIT = serverConfiguration.getSERVER_MEMORY_POOL_UNIT();

		double MEMORY_POOL_SIZE = new BigDecimal(SERVER_MEMORY_POOL_CAPACITY * SERVER_MEMORY_POOL_UNIT)
				.divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP).doubleValue();

		this.encoding = serverConfiguration.getSERVER_ENCODING();
		this.sessionIdleTime = serverConfiguration.getSERVER_SESSION_IDLE_TIME();

		this.protocolEncoder = protocolFactory.getProtocolEncoder();

		this.mcByteBufAllocator = new MCByteBufAllocator(this);

		this.addSessionEventListener(new SocketSessionManagerSEListener());

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

		int eventQueueSize = serverConfiguration.getSERVER_IO_EVENT_QUEUE();

		int eventLoopSize = serverConfiguration.getSERVER_CORE_SIZE();
		
		if (serverConfiguration.isSERVER_ENABLE_WORK_EVENT_LOOP()) {
			this.eventLoopGroup = new ThreadEventLoopGroup("event-process", eventQueueSize, eventLoopSize);
		} else {
			this.eventLoopGroup = new LineEventLoopGroup("event-process", eventQueueSize, eventLoopSize);
		}

		this.foreReadFutureAcceptor = new EventLoopReadFutureAcceptor();

		this.channelByteBufReader = new IoLimitChannelByteBufReader();

		if (enableSSL) {
			getLastChannelByteBufReader(channelByteBufReader).setNext(new SslChannelByteBufReader());
		}

		getLastChannelByteBufReader(channelByteBufReader).setNext(new TransparentByteBufReader(this));

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

	protected void doStop() throws Exception {

		CloseUtil.close(sessionManager);

		LifeCycleUtil.stop(eventLoopGroup);

		LifeCycleUtil.stop(ioEventHandleAdaptor);

		LifeCycleUtil.stop(mcByteBufAllocator);
	}

	public ProtocolFactory getProtocolFactory() {
		return protocolFactory;
	}

	public IoEventHandleAdaptor getIoEventHandleAdaptor() {
		return ioEventHandleAdaptor;
	}

	public EventLoopGroup getEventLoopGroup() {
		return eventLoopGroup;
	}

	public void setIoEventHandleAdaptor(IoEventHandleAdaptor ioEventHandleAdaptor) {
		this.ioEventHandleAdaptor = ioEventHandleAdaptor;
	}

	public void setProtocolFactory(ProtocolFactory protocolFactory) {
		this.protocolFactory = protocolFactory;
	}

	public SslContext getSslContext() {
		return sslContext;
	}

	public void setSslContext(SslContext sslContext) {
		if (sslContext == null) {
			throw new IllegalArgumentException("null sslContext");
		}
		this.sslContext = sslContext;
		this.enableSSL = true;
		this.sslContext.initialize(this);
	}

	public boolean isEnableSSL() {
		return enableSSL;
	}

	public SocketSessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSocketSessionFactory(SocketSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public ChannelByteBufReader getChannelByteBufReader() {
		return channelByteBufReader;
	}

	public ForeReadFutureAcceptor getForeReadFutureAcceptor() {
		return foreReadFutureAcceptor;
	}

	public void setSessionManager(SocketSessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

}
