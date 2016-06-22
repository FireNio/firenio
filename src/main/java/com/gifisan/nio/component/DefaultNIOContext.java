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
import com.gifisan.nio.common.SharedBundle;
import com.gifisan.nio.component.protocol.DefaultTCPProtocolDecoder;
import com.gifisan.nio.component.protocol.DefaultTCPProtocolEncoder;
import com.gifisan.nio.component.protocol.ProtocolDecoder;
import com.gifisan.nio.component.protocol.ProtocolEncoder;
import com.gifisan.nio.concurrent.ExecutorThreadPool;
import com.gifisan.nio.concurrent.ThreadPool;
import com.gifisan.nio.server.NIOContext;
import com.gifisan.nio.server.ServerProtocolDecoder;
import com.gifisan.nio.server.ServerReadFutureAcceptor;
import com.gifisan.nio.server.ServerUDPEndPointFactory;
import com.gifisan.nio.server.SessionFactory;
import com.gifisan.nio.server.configuration.ServerConfiguration;

public class DefaultNIOContext extends AbstractLifeCycle implements NIOContext {

	private Map<String, Object>		attributes			= new HashMap<String, Object>();
	private DatagramPacketAcceptor	datagramPacketAcceptor	= null;
	private Charset				encoding				= Encoding.DEFAULT;
	private Logger					logger				= LoggerFactory.getLogger(DefaultNIOContext.class);
	private ProtocolDecoder			protocolDecoder		= new DefaultTCPProtocolDecoder();
	private ProtocolEncoder			protocolEncoder		= new DefaultTCPProtocolEncoder();
	private ReadFutureAcceptor		readFutureAcceptor		= null;
	private ServerConfiguration		serverConfiguration		= null;
	private ThreadPool				threadPool			= null;
	private SessionFactory			sessionFactory			= new SessionFactory();
	private UDPEndPointFactory		udpEndPointFactory		= null;
	private IOEventHandle			ioEventHandle			= null;
	private IOService				tcpIOService			= null;
	private IOService				udpIOService			= null;

	public DefaultNIOContext(ProtocolDecoder protocolDecoder, ProtocolEncoder protocolEncoder,
			IOEventHandle ioEventHandle) {
		this.protocolDecoder = protocolDecoder;
		this.protocolEncoder = protocolEncoder;
		this.ioEventHandle = ioEventHandle;
	}

	public void clearAttributes() {
		this.attributes.clear();
	}

	protected void doStart() throws Exception {

		SharedBundle bundle = SharedBundle.instance();

		if (serverConfiguration == null) {
			this.serverConfiguration = loadServerConfiguration(bundle);
		}

		int SERVER_CORE_SIZE = serverConfiguration.getSERVER_CORE_SIZE();

		Charset encoding = serverConfiguration.getSERVER_ENCODING();

		Encoding.DEFAULT = encoding;

		this.encoding = Encoding.DEFAULT;
		this.threadPool = new ExecutorThreadPool("IOEvent-Executor", SERVER_CORE_SIZE);
		this.readFutureAcceptor = new ServerReadFutureAcceptor();
		this.protocolDecoder = new ServerProtocolDecoder();
		this.udpEndPointFactory = new ServerUDPEndPointFactory();

		logger.info("[NIOServer] ======================================= 服务开始启动 =======================================");
		logger.info("[NIOServer] 项目编码：  { {} }", encoding);
		logger.info("[NIOServer] 监听端口：  { {} }", serverConfiguration.getSERVER_PORT());
		logger.info("[NIOServer] CPU核心数：{ {} }", SERVER_CORE_SIZE);

		this.threadPool.start();
	}

	private ServerConfiguration loadServerConfiguration(SharedBundle bundle) {

		ServerConfiguration configuration = new ServerConfiguration();

		String encoding = bundle.getProperty("SERVER.ENCODING", "GBK");

		configuration.setSERVER_CORE_SIZE(Runtime.getRuntime().availableProcessors());
		configuration.setSERVER_DEBUG(bundle.getBooleanProperty("SERVER.DEBUG"));
		configuration.setSERVER_HOST(bundle.getProperty("SERVER.HOST"));
		configuration.setSERVER_PORT(bundle.getIntegerProperty("SERVER.PORT"));
		configuration.setSERVER_UDP_BOOT(bundle.getBooleanProperty("SERVER.UDP_BOOT"));
		configuration.setSERVER_ENCODING(Charset.forName(encoding));

		return configuration;
	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(threadPool);
	}

	public Object getAttribute(String key) {
		return this.attributes.get(key);
	}

	public Set<String> getAttributeNames() {
		return this.attributes.keySet();
	}

	public DatagramPacketAcceptor getDatagramPacketAcceptor() {
		return datagramPacketAcceptor;
	}

	public Charset getEncoding() {
		return encoding;
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

	public UDPEndPointFactory getUDPEndPointFactory() {
		return udpEndPointFactory;
	}

	public Object removeAttribute(String key) {
		return this.attributes.remove(key);
	}

	public void setAttribute(String key, Object value) {
		this.attributes.put(key, value);
	}

	public void setDatagramPacketAcceptor(DatagramPacketAcceptor datagramPacketAcceptor) {

		if (datagramPacketAcceptor == null) {
			throw new IllegalArgumentException("null");
		}

		if (this.datagramPacketAcceptor != null) {
			throw new IllegalArgumentException("already setted");
		}

		this.datagramPacketAcceptor = datagramPacketAcceptor;
	}

	public IOEventHandle getIOEventHandle() {
		return ioEventHandle;
	}

	public ThreadPool getThreadPool() {
		return threadPool;
	}

	public void setUDPEndPointFactory(UDPEndPointFactory udpEndPointFactory) {
		this.udpEndPointFactory = udpEndPointFactory;
	}

	public IOService getTCPIOService() {
		return tcpIOService;
	}

	public void setTCPIOService(IOService tcpIOService) {
		this.tcpIOService = tcpIOService;
	}

	public IOService getUDPIOService() {
		return udpIOService;
	}

	public void setUDPIOService(IOService udpIOService) {
		this.udpIOService = udpIOService;
	}

}
