package com.gifisan.mtp.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.mtp.AbstractLifeCycleListener;
import com.gifisan.mtp.LifeCycle;
import com.gifisan.mtp.LifeCycleListener;
import com.gifisan.mtp.component.ServletService;

public class MTPServerListener extends AbstractLifeCycleListener implements LifeCycleListener {

	private final Logger logger = LoggerFactory.getLogger(ServletService.class);
	
	private long staredTime = 0;
	
	public int lifeCycleListenerSortIndex() {
		return 999;
	}
	
	public void lifeCycleStarting(LifeCycle lifeCycle) {
		staredTime = System.currentTimeMillis();
	}

	public void lifeCycleStarted(LifeCycle lifeCycle) {
		MTPServer server = (MTPServer) lifeCycle;
		Connector connector = server.getConnector();
		logger.info("[MTPServer] 服务启动完成  @"+connector.getHost()+":"+connector.getPort()
				+" 花费 "+(System.currentTimeMillis() - staredTime) +" 毫秒");
		staredTime = System.currentTimeMillis();
	}

	public void lifeCycleFailure(LifeCycle lifeCycle, Exception exception) {
		//NIOConnector connector = (NIOConnector) lifeCycle;
		exception.printStackTrace();
	}

	public void lifeCycleStopped(LifeCycle lifeCycle) {
		logger.info("[MTPServer] 服务停止成功");
	}

	public void lifeCycleStopping(LifeCycle lifeCycle) {
		MTPServer server = (MTPServer) lifeCycle;
		Connector connector = server.getConnector();
		logger.info("[MTPServer] 服务运行时间  @"+connector.getHost()+":"+connector.getPort()
				+" 共 "+(System.currentTimeMillis() - staredTime) +" 毫秒");
		logger.info("[MTPServer] 开始停止服务，请稍等");
	}
	
	
	

}
