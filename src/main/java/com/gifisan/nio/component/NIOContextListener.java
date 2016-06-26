package com.gifisan.nio.component;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.gifisan.nio.AbstractLifeCycleListener;
import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.LifeCycleListener;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.LoggerUtil;
import com.gifisan.nio.extend.configuration.ServerConfiguration;

public class NIOContextListener extends AbstractLifeCycleListener implements LifeCycleListener {

	private Logger		logger		= LoggerFactory.getLogger(NIOContextListener.class);
	private long		staredTime	;

	public int lifeCycleListenerSortIndex() {
		return 999;
	}

	public void lifeCycleStarting(LifeCycle lifeCycle) {
		staredTime = System.currentTimeMillis();
	}

	public void lifeCycleStarted(LifeCycle lifeCycle) {
		NIOContext context = (NIOContext) lifeCycle;
		ServerConfiguration configuration = context.getServerConfiguration();
		LoggerUtil.prettyNIOServerLog(logger, "服务启动完成  @127.0.0.1:" + configuration.getSERVER_TCP_PORT() + " 花费 "
				+ (System.currentTimeMillis() - staredTime) + " 毫秒");
	}

	public void lifeCycleFailure(LifeCycle lifeCycle, Exception exception) {
		// NIOConnector connector = (NIOConnector) lifeCycle;
		logger.error(exception.getMessage(), exception);
		;
	}

	public void lifeCycleStopped(LifeCycle lifeCycle) {
		LoggerUtil.prettyNIOServerLog(logger, "服务停止成功");
	}

	public void lifeCycleStopping(LifeCycle lifeCycle) {
		NIOContext context = (NIOContext) lifeCycle;
		
		if (context == null) {
			LoggerUtil.prettyNIOServerLog(logger, "服务启动失败，正在停止...");
			return;
		}
		
		if (context.getTCPService() == null) {
			LoggerUtil.prettyNIOServerLog(logger, "服务启动失败，正在停止...");
			return;
		}
		
		ServerConfiguration configuration = context.getServerConfiguration();
		
		BigDecimal time = new BigDecimal(System.currentTimeMillis() - staredTime);
		BigDecimal anHour = new BigDecimal(60 * 60 * 1000);
		BigDecimal hour = time.divide(anHour, 3, RoundingMode.HALF_UP);
		String[] params = { String.valueOf(configuration.getSERVER_TCP_PORT()), String.valueOf(hour) };
		LoggerUtil.prettyNIOServerLog(logger, "服务运行时间  @127.0.0.1:{} 共 {} 小时", params);
		LoggerUtil.prettyNIOServerLog(logger, "开始停止服务，请稍等");
	}

}
