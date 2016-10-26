package com.generallycloud.nio.component;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.generallycloud.nio.AbstractLifeCycle;
import com.generallycloud.nio.acceptor.DatagramChannelFactory;
import com.generallycloud.nio.buffer.ByteBufferPool;
import com.generallycloud.nio.buffer.HeapMemoryPoolV3;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.common.ssl.SslContext;
import com.generallycloud.nio.component.concurrent.EventLoopGroup;
import com.generallycloud.nio.component.concurrent.EventLoopThread;
import com.generallycloud.nio.component.concurrent.SingleEventLoopGroup;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public class BaseContextImpl extends AbstractLifeCycle implements BaseContext {

	private Map<Object, Object>			attributes	= new HashMap<Object, Object>();
	private Sequence					sequence		= new Sequence();
	private DatagramPacketAcceptor		datagramPacketAcceptor;
	private Charset					encoding;
	private IOEventHandleAdaptor			ioEventHandleAdaptor;
	private SessionEventListenerWrapper	lastSessionEventListener;
	private Logger						logger		= LoggerFactory.getLogger(BaseContextImpl.class);
	private ServerConfiguration			serverConfiguration;
	private SessionEventListenerWrapper	sessionEventListenerStub;
	private SessionFactory				sessionFactory;
	private ChannelService				socketChannelService;
	private DatagramChannelFactory		datagramChannelFactory;
	private ChannelService				datagramChannelService;
	private ProtocolFactory				protocolFactory;
	private EventLoopThread				sessionFactoryThread;
	private long						sessionIdleTime;
	private BeatFutureFactory			beatFutureFactory;
	private int						sessionAttachmentSize;
	private long						startupTime	= System.currentTimeMillis();
	private EventLoopGroup				eventLoopGroup;
	private ByteBufferPool				heapByteBufferPool;
	private ProtocolEncoder				protocolEncoder;
	private SslContext					sslContext;
	private boolean 					enableSSL;

	// private ByteBufferPool directByteBufferPool;

	public int getSessionAttachmentSize() {
		return sessionAttachmentSize;
	}

	public void setSessionAttachmentSize(int sessionAttachmentSize) {
		this.sessionAttachmentSize = sessionAttachmentSize;
	}

	public BeatFutureFactory getBeatFutureFactory() {
		return beatFutureFactory;
	}

	public ByteBufferPool getHeapByteBufferPool() {
		return heapByteBufferPool;
	}

	// public ByteBufferPool getDirectByteBufferPool() {
	// return directByteBufferPool;
	// }

	public void setBeatFutureFactory(BeatFutureFactory beatFutureFactory) {
		this.beatFutureFactory = beatFutureFactory;
	}

	public ProtocolEncoder getProtocolEncoder() {
		return protocolEncoder;
	}

	public BaseContextImpl(ServerConfiguration configuration) {

		if (configuration == null) {
			throw new IllegalArgumentException("null configuration");
		}

		int eventQueueSize = configuration.getSERVER_CHANNEL_QUEUE_SIZE();

		int eventLoopSize = configuration.getSERVER_CORE_SIZE();

		EventLoopGroup eventLoopGroup = new SingleEventLoopGroup("IOEvent", eventQueueSize, eventLoopSize);

		this.serverConfiguration = configuration;

		this.eventLoopGroup = eventLoopGroup;

		this.addLifeCycleListener(new BaseContextListener());
	}

	public BaseContextImpl(ServerConfiguration configuration, EventLoopGroup eventLoopGroup) {

		if (configuration == null) {
			throw new IllegalArgumentException("null configuration");
		}

		if (eventLoopGroup == null) {
			throw new IllegalArgumentException("null eventLoopGroup");
		}

		this.serverConfiguration = configuration;

		this.eventLoopGroup = eventLoopGroup;

		this.addLifeCycleListener(new BaseContextListener());
	}

	public void addSessionEventListener(SessionEventListener listener) {
		if (this.sessionEventListenerStub == null) {
			this.sessionEventListenerStub = new SessionEventListenerWrapper(listener);
			this.lastSessionEventListener = this.sessionEventListenerStub;
		} else {
			this.lastSessionEventListener.setNext(new SessionEventListenerWrapper(listener));
			this.lastSessionEventListener = this.lastSessionEventListener.nextListener();
		}
	}

	public void clearAttributes() {
		this.attributes.clear();
	}

	protected void doStart() throws Exception {

		if (ioEventHandleAdaptor == null) {
			throw new IllegalArgumentException("null ioEventHandle");
		}

		if (protocolFactory == null) {
			throw new IllegalArgumentException("null protocolFactory");
		}

		int SERVER_CORE_SIZE = serverConfiguration.getSERVER_CORE_SIZE();

		int SERVER_CHANNEL_QUEUE_SIZE = serverConfiguration.getSERVER_CHANNEL_QUEUE_SIZE();

		int SERVER_MEMORY_POOL_CAPACITY = serverConfiguration.getSERVER_MEMORY_POOL_CAPACITY();
		int SERVER_MEMORY_POOL_UNIT = serverConfiguration.getSERVER_MEMORY_POOL_UNIT();

		double MEMORY_POOL_SIZE = new BigDecimal(SERVER_MEMORY_POOL_CAPACITY * SERVER_MEMORY_POOL_UNIT).divide(
				new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP).doubleValue();

		this.encoding = serverConfiguration.getSERVER_ENCODING();
		this.sessionIdleTime = serverConfiguration.getSERVER_SESSION_IDLE_TIME();

		this.datagramChannelFactory = new DatagramChannelFactory();
		this.protocolEncoder = protocolFactory.getProtocolEncoder();
		
		this.heapByteBufferPool = new HeapMemoryPoolV3(SERVER_MEMORY_POOL_CAPACITY, SERVER_MEMORY_POOL_UNIT);
		// this.directByteBufferPool = new
		// DirectMemoryPoolV3(SERVER_MEMORY_POOL_CAPACITY,SERVER_MEMORY_POOL_UNIT);

		this.addSessionEventListener(new ManagerSEListener());

		LoggerUtil.prettyNIOServerLog(logger,
				"======================================= 服务开始启动 =======================================");
		LoggerUtil.prettyNIOServerLog(logger, "项目编码           ：{ {} }", encoding);
		LoggerUtil.prettyNIOServerLog(logger, "协议名称           ：{ {} }", protocolFactory.getProtocolID());
		LoggerUtil.prettyNIOServerLog(logger, "CPU核心数          ：{ CPU * {} }", SERVER_CORE_SIZE);
		LoggerUtil.prettyNIOServerLog(logger, "SESSION_IDLE       ：{ {} }",
				serverConfiguration.getSERVER_SESSION_IDLE_TIME());
		LoggerUtil.prettyNIOServerLog(logger, "监听端口(TCP)      ：{ {} }", serverConfiguration.getSERVER_TCP_PORT());
		if (serverConfiguration.getSERVER_UDP_PORT() != 0) {
			LoggerUtil.prettyNIOServerLog(logger, "监听端口(UDP)      ：{ {} }", serverConfiguration.getSERVER_UDP_PORT());
		}
		LoggerUtil.prettyNIOServerLog(logger, "写入缓冲区         ：{ {} * {} }", SERVER_CHANNEL_QUEUE_SIZE,
				SERVER_CORE_SIZE);
		LoggerUtil.prettyNIOServerLog(logger, "内存池容量         ：{ {} * {} ≈ {} M }", new Object[] {
				SERVER_MEMORY_POOL_UNIT, SERVER_MEMORY_POOL_CAPACITY, MEMORY_POOL_SIZE });

		LifeCycleUtil.start(ioEventHandleAdaptor);

		if (sessionFactory == null) {
			sessionFactory = new SessionFactory(this);
		}

		this.heapByteBufferPool.start();

		// this.directByteBufferPool.start();

		this.sessionFactoryThread = new EventLoopThread(sessionFactory, "session-manager");

		this.sessionFactoryThread.start();

		this.eventLoopGroup.start();
	}

	protected void doStop() throws Exception {
		
		LifeCycleUtil.stop(eventLoopGroup);

		LifeCycleUtil.stop(ioEventHandleAdaptor);

		LifeCycleUtil.stop(sessionFactoryThread);

		LifeCycleUtil.stop(heapByteBufferPool);

		// LifeCycleUtil.stop(directByteBufferPool);
	}

	public ProtocolFactory getProtocolFactory() {
		return protocolFactory;
	}

	public Object getAttribute(Object key) {
		return this.attributes.get(key);
	}

	public Set<Object> getAttributeNames() {
		return this.attributes.keySet();
	}

	public DatagramPacketAcceptor getDatagramPacketAcceptor() {
		return datagramPacketAcceptor;
	}

	public Charset getEncoding() {
		return encoding;
	}

	public IOEventHandleAdaptor getIOEventHandleAdaptor() {
		return ioEventHandleAdaptor;
	}

	public ServerConfiguration getServerConfiguration() {
		return serverConfiguration;
	}

	public SessionEventListenerWrapper getSessionEventListenerStub() {
		return sessionEventListenerStub;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public ChannelService getTCPService() {
		return socketChannelService;
	}

	public EventLoopGroup getEventLoopGroup() {
		return eventLoopGroup;
	}

	public DatagramChannelFactory getDatagramChannelFactory() {
		return datagramChannelFactory;
	}

	public ChannelService getUDPService() {
		return datagramChannelService;
	}

	public Object removeAttribute(Object key) {
		return this.attributes.remove(key);
	}

	public void setAttribute(Object key, Object value) {
		this.attributes.put(key, value);
	}

	public void setDatagramPacketAcceptor(DatagramPacketAcceptor datagramPacketAcceptor) {
		this.datagramPacketAcceptor = datagramPacketAcceptor;
	}

	public void setIOEventHandleAdaptor(IOEventHandleAdaptor ioEventHandleAdaptor) {
		this.ioEventHandleAdaptor = ioEventHandleAdaptor;
	}

	public void setDatagramChannelFactory(DatagramChannelFactory datagramChannelFactory) {
		this.datagramChannelFactory = datagramChannelFactory;
	}

	public ChannelService getSocketChannelService() {
		return socketChannelService;
	}

	public void setSocketChannelService(ChannelService service) {
		this.socketChannelService = service;
	}

	public ChannelService getDatagramChannelService() {
		return datagramChannelService;
	}

	public void setDatagramChannelService(ChannelService service) {
		this.datagramChannelService = service;
	}

	public Sequence getSequence() {
		return sequence;
	}

	public void setProtocolFactory(ProtocolFactory protocolFactory) {
		this.protocolFactory = protocolFactory;
	}

	public long getSessionIdleTime() {
		return sessionIdleTime;
	}

	public long getStartupTime() {
		return startupTime;
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
	
}
