package com.gifisan.mtp.server;

import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.common.SharedBundle;
import com.gifisan.mtp.component.ServerNIOEndPoint;
import com.gifisan.mtp.concurrent.ExecutorThreadPool;
import com.gifisan.mtp.concurrent.TaskExecutor;
import com.gifisan.mtp.schedule.Job;

public class ServerEndpointFactory extends AbstractLifeCycle implements Job {

	private Logger						logger				= LoggerFactory.getLogger(ServerEndpointFactory.class);
	private HashMap<Long, ServerEndPoint>	endPoints				= new HashMap<Long, ServerEndPoint>();
	private TaskExecutor				taskExecutor			= null;
	private ExecutorThreadPool			asynchServletDispatcher	= null;
//	private AtomicLong 					genericID 			= new AtomicLong(10000);
	private long 						genericID 			= 10000;
	private Map<Long, ServerEndPoint>		readOnlyEndPoints 		= Collections.unmodifiableMap(endPoints);

	protected void doStart() throws Exception {
		
		SharedBundle bundle 		= SharedBundle.instance();
//		int CHECK_INTERVAL			= 10;
		int CHECK_INTERVAL			= 60 * 1000;
		int CORE_SIZE 				= bundle.getIntegerProperty("SERVER.CORE_SIZE",4);
		this.taskExecutor 			= new TaskExecutor(this, "EndPoint-manager-Task", CHECK_INTERVAL);
		this.asynchServletDispatcher	= new ExecutorThreadPool(CORE_SIZE,"asynch-servlet-dispatcher-");
		this.asynchServletDispatcher	.start();
		this.taskExecutor			.start();
	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(asynchServletDispatcher);
		this.taskExecutor.stop();
	}
	

	public ServerNIOEndPoint manager(ServerContext context,SelectionKey selectionKey) throws SocketException{
		
		ServerNIOEndPoint endPoint = new ServerNIOEndPoint(context,selectionKey,genericID++);
		
		synchronized (endPoints) {
			endPoints.put(endPoint.getEndPointID(), endPoint);
		}
		
		return endPoint;
		
	}
	
	public void schedule() {
		
		
		
		logger.info("[MTPServer] 回收过期会话，剩余数量：" + endPoints.size());
		
	}
	
	public Map<Long, ServerEndPoint> getManagerdEndPoints(){
		return readOnlyEndPoints;
	}

	public void remove(ServerEndPoint endPoint) {
		Map<Long, ServerEndPoint> endPoints = this.endPoints;
		synchronized (endPoints) {
			endPoints.remove(endPoint.getEndPointID());
			logger.info("EndPoint removed >> "+endPoint.getEndPointID());
		}
		
		
	}

}
