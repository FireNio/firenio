package com.gifisan.nio.server;

import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.component.ServerNIOEndPoint;
import com.gifisan.nio.concurrent.TaskExecutor;

public class ServerEndpointFactory extends AbstractLifeCycle implements Runnable {

	private Logger						logger			= LoggerFactory.getLogger(ServerEndpointFactory.class);
	private HashMap<Long, ServerEndPoint>	endPoints			= new HashMap<Long, ServerEndPoint>();
	private TaskExecutor				taskExecutor		= null;
	private long						genericID			= 10000;
	private Map<Long, ServerEndPoint>		readOnlyEndPoints	= Collections.unmodifiableMap(endPoints);
	private ServerContext				context			= null;

	public ServerEndpointFactory(ServerContext context) {
		this.context = context;
	}

	protected void doStart() throws Exception {
		int CHECK_INTERVAL = 60 * 1000;
		this.taskExecutor = new TaskExecutor(this, "EndPoint-manager", CHECK_INTERVAL);
		this.taskExecutor.start();
	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(taskExecutor);
	}

	public ServerNIOEndPoint manager(ServerContext context, SelectionKey selectionKey) throws SocketException {

		ServerNIOEndPoint endPoint = new ServerNIOEndPoint(context, selectionKey, genericID++);

		endPoints.put(endPoint.getEndPointID(), endPoint);

		return endPoint;

	}

	public void run() {

		if (endPoints.size() == 0) {
			return;
		}
		
		logger.info("[NIOServer] 回收过期会话，剩余数量：" + endPoints.size());

		context.getEncoding();
	}

	public Map<Long, ServerEndPoint> getManagerdEndPoints() {
		return readOnlyEndPoints;
	}

	public void remove(ServerEndPoint endPoint) {
		Map<Long, ServerEndPoint> endPoints = this.endPoints;
		endPoints.remove(endPoint.getEndPointID());
		DebugUtil.debug("EndPoint removed >> " + endPoint.getEndPointID());

	}

}
