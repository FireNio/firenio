package com.yoocent.mtp.server;

import com.yoocent.mtp.AbstractLifeCycleListener;
import com.yoocent.mtp.LifeCycle;
import com.yoocent.mtp.LifeCycleListener;

public class MTPServerListener extends AbstractLifeCycleListener implements LifeCycleListener {

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
		System.out.println("[MTPServer] 服务启动完成  @"+connector.getHost()+":"+connector.getPort()
				+" 花费 "+(System.currentTimeMillis() - staredTime) +" 毫秒");
		staredTime = System.currentTimeMillis();
	}

	public void lifeCycleFailure(LifeCycle lifeCycle, Exception exception) {
		//NIOConnector connector = (NIOConnector) lifeCycle;
		exception.printStackTrace();
	}

	public void lifeCycleStopped(LifeCycle lifeCycle) {
		System.out.println("[MTPServer] 服务停止成功");
	}

	public void lifeCycleStopping(LifeCycle lifeCycle) {
		MTPServer server = (MTPServer) lifeCycle;
		Connector connector = server.getConnector();
		System.out.println("[MTPServer] 服务运行时间  @"+connector.getHost()+":"+connector.getPort()
				+" 共 "+(System.currentTimeMillis() - staredTime) +" 毫秒");
		System.out.println("[MTPServer] 开始停止服务，请稍等");
	}
	
	
	

}
