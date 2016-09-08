package com.generallycloud.nio.component;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.generallycloud.nio.AbstractLifeCycle;
import com.generallycloud.nio.Encoding;
import com.generallycloud.nio.acceptor.UDPEndPointFactory;
import com.generallycloud.nio.buffer.ByteBufferPool;
import com.generallycloud.nio.buffer.DirectMemoryPool;
import com.generallycloud.nio.buffer.HeapMemoryPool;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.component.concurrent.EventLoopGroup;
import com.generallycloud.nio.component.concurrent.UniqueThread;
import com.generallycloud.nio.component.protocol.ProtocolFactory;
import com.generallycloud.nio.configuration.ServerConfiguration;

public class DefaultNIOContext extends AbstractLifeCycle implements NIOContext {

	private Map<Object, Object>			attributes		= new HashMap<Object, Object>();
	private Sequence					sequence			= new Sequence();
	private DatagramPacketAcceptor		datagramPacketAcceptor;
	private Charset					encoding			;
	private IOEventHandleAdaptor			ioEventHandleAdaptor;
	private SessionEventListenerWrapper	lastSessionEventListener;
	private Logger						logger			= LoggerFactory.getLogger(DefaultNIOContext.class);
	private IOReadFutureAcceptor			ioReadFutureAcceptor;
	private ServerConfiguration			serverConfiguration;
	private SessionEventListenerWrapper	sessionEventListenerStub;
	private SessionFactory				sessionFactory;
	private IOService					tcpService;
	private UDPEndPointFactory			udpEndPointFactory;
	private IOService					udpService;
	private ProtocolFactory				protocolFactory;
	private UniqueThread				sessionFactoryThread;
	private long						sessionIdleTime	;
	private BeatFutureFactory			beatFutureFactory;
	private int						sessionAttachmentSize;
	private long 						startupTime		= System.currentTimeMillis();
	private boolean					isAcceptBeat;
	private EventLoopGroup				eventLoopGroup;
	private ByteBufferPool				heapByteBufferPool;
	private ByteBufferPool				directByteBufferPool;

	public boolean isAcceptBeat() {
		return isAcceptBeat;
	}
	
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

	public ByteBufferPool getDirectByteBufferPool() {
		return directByteBufferPool;
	}

	public void setBeatFutureFactory(BeatFutureFactory beatFutureFactory) {
		this.beatFutureFactory = beatFutureFactory;
	}

	public DefaultNIOContext(ServerConfiguration configuration,EventLoopGroup eventLoopGroup) {
		
		if (configuration == null) {
			throw new IllegalArgumentException("null configuration");
		}
		
		if (eventLoopGroup == null) {
			throw new IllegalArgumentException("null eventLoopGroup");
		}
		
		this.serverConfiguration = configuration;
		
		this.eventLoopGroup = eventLoopGroup;
		
		this.addLifeCycleListener(new NIOContextListener());
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

		Charset encoding = serverConfiguration.getSERVER_ENCODING();

		Encoding.DEFAULT = encoding;

		this.encoding = Encoding.DEFAULT;
		this.isAcceptBeat = serverConfiguration.isSERVER_IS_ACCEPT_BEAT();
		this.sessionIdleTime = serverConfiguration.getSERVER_SESSION_IDLE_TIME();
		
		this.ioReadFutureAcceptor = new IOReadFutureDispatcher();
		this.udpEndPointFactory = new UDPEndPointFactory();
		
		int bCapacity = 1024;
		
//		this.heapByteBufferPool = new HeapByteBufferPool(bCapacity * 100);
//		this.directByteBufferPool = new DirectByteBufferPool(bCapacity * 100);
		
		this.heapByteBufferPool = new HeapMemoryPool(bCapacity);
		this.directByteBufferPool = new DirectMemoryPool(bCapacity);
		
		this.addSessionEventListener(new ManagerSEListener());

		LoggerUtil.prettyNIOServerLog(logger,
				"======================================= 服务开始启动 =======================================");
		LoggerUtil.prettyNIOServerLog(logger, "项目编码           ：{ {} }", encoding);
		LoggerUtil.prettyNIOServerLog(logger, "监听端口(TCP)      ：{ {} }", serverConfiguration.getSERVER_TCP_PORT());
		LoggerUtil.prettyNIOServerLog(logger, "写入缓冲区(EDPW)   ：{ {} * {} }", SERVER_CHANNEL_QUEUE_SIZE, SERVER_CORE_SIZE);
		if (serverConfiguration.getSERVER_UDP_PORT() != 0) {
			LoggerUtil.prettyNIOServerLog(logger, "监听端口(UDP)      ：{ {} }", serverConfiguration.getSERVER_UDP_PORT());
		}
		LoggerUtil.prettyNIOServerLog(logger, "CPU核心数          ：{ {} }", SERVER_CORE_SIZE);

		LifeCycleUtil.start(ioEventHandleAdaptor);

		if (sessionFactory == null) {
			sessionFactory = new SessionFactory(this);
		}

		this.heapByteBufferPool.start();
		
		this.directByteBufferPool.start();
		
		this.sessionFactoryThread = new UniqueThread(sessionFactory, "session-manager");

		this.sessionFactoryThread.start();
		
		this.eventLoopGroup.start();
	}

	protected void doStop() throws Exception {

		LifeCycleUtil.stop(ioEventHandleAdaptor);

		LifeCycleUtil.stop(eventLoopGroup);

		LifeCycleUtil.stop(sessionFactoryThread);
		
		LifeCycleUtil.stop(heapByteBufferPool);
		
		LifeCycleUtil.stop(directByteBufferPool);
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

	public IOReadFutureAcceptor getIOReadFutureAcceptor() {
		return ioReadFutureAcceptor;
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

	public IOService getTCPService() {
		return tcpService;
	}

	public EventLoopGroup getEventLoopGroup() {
		return eventLoopGroup;
	}

	public UDPEndPointFactory getUDPEndPointFactory() {
		return udpEndPointFactory;
	}

	public IOService getUDPService() {
		return udpService;
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

	public void setTCPService(IOService tcpService) {
		this.tcpService = tcpService;
	}

	public void setUDPEndPointFactory(UDPEndPointFactory udpEndPointFactory) {
		this.udpEndPointFactory = udpEndPointFactory;
	}

	public void setUDPService(IOService udpService) {
		this.udpService = udpService;
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

}
