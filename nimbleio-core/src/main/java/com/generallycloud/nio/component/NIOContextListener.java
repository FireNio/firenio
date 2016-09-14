package com.generallycloud.nio.component;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.generallycloud.nio.AbstractLifeCycleListener;
import com.generallycloud.nio.LifeCycle;
import com.generallycloud.nio.LifeCycleListener;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.configuration.ServerConfiguration;

public class NIOContextListener extends AbstractLifeCycleListener implements LifeCycleListener {

	private Logger		logger		= LoggerFactory.getLogger(NIOContextListener.class);

	public int lifeCycleListenerSortIndex() {
		return 999;
	}

	public void lifeCycleStarted(LifeCycle lifeCycle) {
//		LoggerUtil.prettyNIOServerLog(logger, "CONTEXT加载完成");
	}

	public void lifeCycleFailure(LifeCycle lifeCycle, Exception exception) {
		// NIOConnector connector = (NIOConnector) lifeCycle;
		logger.error(exception.getMessage(), exception);
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
		
		BigDecimal time = new BigDecimal(System.currentTimeMillis() - context.getStartupTime());
		BigDecimal anHour = new BigDecimal(60 * 60 * 1000);
		BigDecimal hour = time.divide(anHour, 3, RoundingMode.HALF_UP);
		String[] params = { String.valueOf(configuration.getSERVER_TCP_PORT()), String.valueOf(hour) };
		LoggerUtil.prettyNIOServerLog(logger, "服务运行时间  @127.0.0.1:{} 共 {} 小时", params);
		LoggerUtil.prettyNIOServerLog(logger, "开始停止服务，请稍等");
	}

}
