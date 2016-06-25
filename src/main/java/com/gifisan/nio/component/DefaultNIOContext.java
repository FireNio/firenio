package com.gifisan.nio.component;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.Encoding;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.LoggerUtil;
import com.gifisan.nio.common.SharedBundle;
import com.gifisan.nio.component.protocol.DefaultTCPProtocolDecoder;
import com.gifisan.nio.component.protocol.DefaultTCPProtocolEncoder;
import com.gifisan.nio.component.protocol.ProtocolDecoder;
import com.gifisan.nio.component.protocol.ProtocolEncoder;
import com.gifisan.nio.concurrent.ExecutorThreadPool;
import com.gifisan.nio.concurrent.ThreadPool;
import com.gifisan.nio.server.NIOContext;
import com.gifisan.nio.server.NIOContextListener;
import com.gifisan.nio.server.ReadFutureDispatcher;
import com.gifisan.nio.server.ServerUDPEndPointFactory;
import com.gifisan.nio.server.SessionFactory;
import com.gifisan.nio.server.configuration.ServerConfiguration;

public class DefaultNIOContext extends AbstractLifeCycle implements NIOContext {

	private Map<String, Object>			attributes			= new HashMap<String, Object>();
	private Charset					encoding				= Encoding.DEFAULT;
	private IOEventHandle				ioEventHandle			= null;
	private Logger						logger				= LoggerFactory
																.getLogger(DefaultNIOContext.class);
	private ProtocolDecoder				protocolDecoder		= new DefaultTCPProtocolDecoder();
	private ProtocolEncoder				protocolEncoder		= new DefaultTCPProtocolEncoder();
	private ReadFutureAcceptor			readFutureAcceptor		= null;
	private ServerConfiguration			serverConfiguration		= null;
	private SessionFactory				sessionFactory			= new SessionFactory();
	private IOService					tcpService			= null;
	private ThreadPool					threadPool			= null;
	private UDPEndPointFactory			udpEndPointFactory		= null;
	private IOService					udpService			= null;
	private DatagramPacketAcceptor		datagramPacketAcceptor	= null;
	private SessionEventListenerWrapper	lastSessionEventListener	= null;
	private SessionEventListenerWrapper	sessionEventListenerStub	= null;


	public DatagramPacketAcceptor getDatagramPacketAcceptor() {
		return datagramPacketAcceptor;
	}

	public void setDatagramPacketAcceptor(DatagramPacketAcceptor datagramPacketAcceptor) {
		this.datagramPacketAcceptor = datagramPacketAcceptor;
	}

	
	public SessionEventListenerWrapper getSessionEventListenerStub() {
		return sessionEventListenerStub;
	}

	public void addSessionEventListener(SessionEventListener listener) {
		if (this.sessionEventListenerStub == null) {
			this.sessionEventListenerStub = new SessionEventListenerWrapper(listener);
			this.lastSessionEventListener = this.sessionEventListenerStub;
		} else {
			this.lastSessionEventListener.setNext(new SessionEventListenerWrapper(listener));
		}
	}

	public DefaultNIOContext() {
		this.addLifeCycleListener(new NIOContextListener());
	}

	public void clearAttributes() {
		this.attributes.clear();
	}

	protected void doStart() throws Exception {

		SharedBundle bundle = SharedBundle.instance();

		if (ioEventHandle == null) {
			throw new IllegalArgumentException("null ioEventHandle");
		}

		if (serverConfiguration == null) {
			this.serverConfiguration = loadServerConfiguration(bundle);
		}

		if (protocolEncoder == null) {
			protocolEncoder = new DefaultTCPProtocolEncoder();
		}

		if (protocolDecoder == null) {
			protocolDecoder = new DefaultTCPProtocolDecoder();
		}

		int SERVER_CORE_SIZE = serverConfiguration.getSERVER_CORE_SIZE();

		Charset encoding = serverConfiguration.getSERVER_ENCODING();

		Encoding.DEFAULT = encoding;

		this.encoding = Encoding.DEFAULT;
		this.threadPool = new ExecutorThreadPool("IOEvent-Executor", SERVER_CORE_SIZE);
		this.readFutureAcceptor = new ReadFutureDispatcher();
		this.udpEndPointFactory = new ServerUDPEndPointFactory();

		LoggerUtil.prettyNIOServerLog(logger, "======================================= 服务开始启动 =======================================");
		LoggerUtil.prettyNIOServerLog(logger, "项目编码     ：  { {} }", encoding);
		LoggerUtil.prettyNIOServerLog(logger, "监听端口(TCP)：  { {} }", serverConfiguration.getSERVER_TCP_PORT());
		if (serverConfiguration.getSERVER_UDP_PORT() != 0) {
			LoggerUtil.prettyNIOServerLog(logger, "监听端口(UDP)：  { {} }", serverConfiguration.getSERVER_UDP_PORT());
		}
		LoggerUtil.prettyNIOServerLog(logger, "CPU核心数    ：  { {} }", SERVER_CORE_SIZE);

		this.ioEventHandle.start();
		this.threadPool.start();
	}

	protected void doStop() throws Exception {

		LifeCycleUtil.stop(ioEventHandle);
		
		LifeCycleUtil.stop(threadPool);
	}

	public Object getAttribute(String key) {
		return this.attributes.get(key);
	}

	public Set<String> getAttributeNames() {
		return this.attributes.keySet();
	}

	public Charset getEncoding() {
		return encoding;
	}

	public IOEventHandle getIOEventHandle() {
		return ioEventHandle;
	}

	public ProtocolDecoder getProtocolDecoder() {
		return protocolDecoder;
	}

	public ProtocolEncoder getProtocolEncoder() {
		return protocolEncoder;
	}

	public ReadFutureAcceptor getReadFutureAcceptor() {
		return readFutureAcceptor;
	}

	public ServerConfiguration getServerConfiguration() {
		return serverConfiguration;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public IOService getTCPService() {
		return tcpService;
	}

	public ThreadPool getThreadPool() {
		return threadPool;
	}

	public UDPEndPointFactory getUDPEndPointFactory() {
		return udpEndPointFactory;
	}

	public IOService getUDPService() {
		return udpService;
	}

	private ServerConfiguration loadServerConfiguration(SharedBundle bundle) {

		ServerConfiguration configuration = new ServerConfiguration();

		String encoding = bundle.getProperty("SERVER.ENCODING", "GBK");

		configuration.setSERVER_CORE_SIZE(Runtime.getRuntime().availableProcessors());
		configuration.setSERVER_DEBUG(bundle.getBooleanProperty("SERVER.DEBUG"));
		configuration.setSERVER_HOST(bundle.getProperty("SERVER.HOST"));
		configuration.setSERVER_TCP_PORT(bundle.getIntegerProperty("SERVER.TCP_PORT"));
		configuration.setSERVER_UDP_PORT(bundle.getIntegerProperty("SERVER.UDP_PORT"));
		configuration.setSERVER_ENCODING(Charset.forName(encoding));

		return configuration;
	}

	public Object removeAttribute(String key) {
		return this.attributes.remove(key);
	}

	public void setAttribute(String key, Object value) {
		this.attributes.put(key, value);
	}

	public void setIOEventHandle(IOEventHandle ioEventHandle) {
		this.ioEventHandle = ioEventHandle;
	}

	public void setProtocolDecoder(ProtocolDecoder protocolDecoder) {
		this.protocolDecoder = protocolDecoder;
	}

	public void setProtocolEncoder(ProtocolEncoder protocolEncoder) {
		this.protocolEncoder = protocolEncoder;
	}

	public void setServerConfiguration(ServerConfiguration serverConfiguration) {
		this.serverConfiguration = serverConfiguration;
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

}
