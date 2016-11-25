package com.generallycloud.nio.component;

import java.math.BigDecimal;
import java.nio.charset.Charset;

import com.generallycloud.nio.acceptor.DatagramChannelFactory;
import com.generallycloud.nio.buffer.MCByteBufAllocator;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.configuration.ServerConfiguration;

public class DatagramChannelContextImpl extends AbstractChannelContext implements DatagramChannelContext {

	private DatagramPacketAcceptor		datagramPacketAcceptor;
	private Charset					encoding;
	private SessionManager				sessionManager;
	private DatagramChannelFactory		datagramChannelFactory;
	private Logger						logger		= LoggerFactory.getLogger(DatagramChannelContextImpl.class);

	public DatagramChannelContextImpl(ServerConfiguration configuration) {
		super(configuration);
	}

	protected void doStart() throws Exception {

		serverConfiguration.initializeDefault(this);

		int SERVER_CORE_SIZE = serverConfiguration.getSERVER_CORE_SIZE();

		long SERVER_MEMORY_POOL_CAPACITY = serverConfiguration.getSERVER_MEMORY_POOL_CAPACITY() * SERVER_CORE_SIZE;
		long SERVER_MEMORY_POOL_UNIT = serverConfiguration.getSERVER_MEMORY_POOL_UNIT();

		double MEMORY_POOL_SIZE = new BigDecimal(SERVER_MEMORY_POOL_CAPACITY * SERVER_MEMORY_POOL_UNIT)
				.divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP).doubleValue();

		this.encoding = serverConfiguration.getSERVER_ENCODING();
		this.sessionIdleTime = serverConfiguration.getSERVER_SESSION_IDLE_TIME();

		this.datagramChannelFactory = new DatagramChannelFactory();

		this.mcByteBufAllocator = new MCByteBufAllocator(this);

		this.addSessionEventListener(new ManagerSEListener());

		LoggerUtil.prettyNIOServerLog(logger,
				"======================================= 服务开始启动 =======================================");
		LoggerUtil.prettyNIOServerLog(logger, "项目编码           ：{ {} }", encoding);
		LoggerUtil.prettyNIOServerLog(logger, "CPU核心数          ：{ CPU * {} }", SERVER_CORE_SIZE);
		LoggerUtil.prettyNIOServerLog(logger, "SESSION_IDLE       ：{ {} }",
				serverConfiguration.getSERVER_SESSION_IDLE_TIME());
		LoggerUtil.prettyNIOServerLog(logger, "监听端口(TCP)      ：{ {} }", serverConfiguration.getSERVER_PORT());
		LoggerUtil.prettyNIOServerLog(logger, "内存池容量         ：{ {} * {} ≈ {} M }",
				new Object[] { SERVER_MEMORY_POOL_UNIT, SERVER_MEMORY_POOL_CAPACITY, MEMORY_POOL_SIZE });

		LifeCycleUtil.start(mcByteBufAllocator);
	}

	protected void doStop() throws Exception {

		CloseUtil.close(sessionManager);

		LifeCycleUtil.stop(mcByteBufAllocator);
	}

	public DatagramPacketAcceptor getDatagramPacketAcceptor() {
		return datagramPacketAcceptor;
	}

	public DatagramChannelFactory getDatagramChannelFactory() {
		return datagramChannelFactory;
	}

	public void setDatagramPacketAcceptor(DatagramPacketAcceptor datagramPacketAcceptor) {
		this.datagramPacketAcceptor = datagramPacketAcceptor;
	}

	public void setDatagramChannelFactory(DatagramChannelFactory datagramChannelFactory) {
		this.datagramChannelFactory = datagramChannelFactory;
	}

}
