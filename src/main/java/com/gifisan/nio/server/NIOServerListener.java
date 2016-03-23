package com.gifisan.nio.server;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.nio.AbstractLifeCycleListener;
import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.LifeCycleListener;

public class NIOServerListener extends AbstractLifeCycleListener implements LifeCycleListener {

	private Logger	logger		= LoggerFactory.getLogger(NIOServerListener.class);
	private long	staredTime	= 0;

	public int lifeCycleListenerSortIndex() {
		return 999;
	}

	public void lifeCycleStarting(LifeCycle lifeCycle) {
		staredTime = System.currentTimeMillis();
	}

	public void lifeCycleStarted(LifeCycle lifeCycle) {
		NIOServer server = (NIOServer) lifeCycle;
		Connector connector = server.getConnector();
		logger.info("  [NIOServer] 服务启动完成  @" + connector.getHost() + ":" + connector.getPort() + " 花费 "
				+ (System.currentTimeMillis() - staredTime) + " 毫秒");
	}

	public void lifeCycleFailure(LifeCycle lifeCycle, Exception exception) {
		// NIOConnector connector = (NIOConnector) lifeCycle;
		logger.error(exception.getMessage(), exception);
		;
	}

	public void lifeCycleStopped(LifeCycle lifeCycle) {
		logger.info("  [NIOServer] 服务停止成功");
	}

	public void lifeCycleStopping(LifeCycle lifeCycle) {
		NIOServer server = (NIOServer) lifeCycle;
		Connector connector = server.getConnector();
		BigDecimal time = new BigDecimal(System.currentTimeMillis() - staredTime);
		BigDecimal anHour = new BigDecimal(60 * 60 * 1000);
		BigDecimal hour = time.divide(anHour, 3, RoundingMode.HALF_UP);
		Object[] params = { connector.getHost(), connector.getPort(), hour };
		logger.info("  [NIOServer] 服务运行时间  @{}:{} 共 {} 小时", params);
		logger.info("  [NIOServer] 开始停止服务，请稍等");
	}

}
