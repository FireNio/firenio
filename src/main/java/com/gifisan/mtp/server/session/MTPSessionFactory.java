package com.gifisan.mtp.server.session;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.common.SharedBundle;
import com.gifisan.mtp.component.ServletService;
import com.gifisan.mtp.concurrent.ExecutorThreadPool;
import com.gifisan.mtp.concurrent.TaskExecutor;
import com.gifisan.mtp.schedule.Job;
import com.gifisan.mtp.server.ServerEndPoint;
import com.gifisan.mtp.server.ServletContext;

public class MTPSessionFactory extends AbstractLifeCycle implements Job {

	private Logger						logger				= LoggerFactory.getLogger(MTPSessionFactory.class);
	private HashMap<Long, InnerSession>	sessions				= new HashMap<Long, InnerSession>();
	private TaskExecutor				taskExecutor			= null;
	private ServletContext				context				= null;
	private ExecutorThreadPool			asynchServletDispatcher	= null;
//	private AtomicLong 					genericID 			= new AtomicLong(10000);
	private long 						genericID 			= 10000;
	private Map<Long, InnerSession>		readOnlySessions 		= Collections.unmodifiableMap(sessions);
	
	public MTPSessionFactory(ServletContext context) {
		this.context = context;
	}

	protected void doStart() throws Exception {
		
		SharedBundle bundle 		= SharedBundle.instance();
//		int CHECK_INTERVAL			= 10;
		int CHECK_INTERVAL			= 30 * 1000;
		int CORE_SIZE 				= bundle.getIntegerProperty("SERVER.CORE_SIZE",4);
		this.taskExecutor 			= new TaskExecutor(this, "Session-manager-Task", CHECK_INTERVAL);
		this.asynchServletDispatcher	= new ExecutorThreadPool(CORE_SIZE,"asynch-servlet-dispatcher-");
		this.asynchServletDispatcher	.start();
		this.taskExecutor			.start();
	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(asynchServletDispatcher);
		this.taskExecutor.stop();
	}

	public InnerSession newSession(ServerEndPoint endPoint,ServletService service) {
		InnerSession session = new MTPSession(
				context, 
				endPoint, 
				this, 
				asynchServletDispatcher, 
				service,
				genericID++);
		
		synchronized (sessions) {
			sessions.put(session.getSessionID(), session);
		}
		return session;
		
		/*
		InnerSession session = sessions.get(sessionID);
		if (session == null) {
			synchronized (sessions) {
				session = sessions.get(sessionID);
				if (session == null) {
					session = new MTPSession(context
							, endPoint
							, sessionID
							, this
							, asynchServletDispatcher
							,new ServletAcceptJobImpl(service));
				}
				sessions.put(sessionID, session);
			}
		} else {
			if (!session.active(endPoint)) {
				logger.info("激活Session失败，这种情况比较少出现，不要看到失败就紧张，后面已经处理了，呵呵");
				synchronized (sessions) {
					sessions.put(sessionID, session);
				}
			}
			session.activeDoor();
			
//			synchronized (session) {
//				if (!session.active(endPoint)) {
//					logger.info("激活Session失败，这种情况比较少出现，不要看到失败就紧张，后面已经处理了，呵呵");
//					testAtomicInteger.incrementAndGet();
//					synchronized (sessions) {
//						sessions.put(sessionID, session);
//					}
//				}
//			}
			
		}
		return session;
		*/
	}
	
	public void schedule() {
		/*
		Map<Long, InnerSession> sessions = this.sessions;
		Collection<InnerSession> sessionCollection = sessions.values();
		InnerSession[] sessionArray = null;
		synchronized (sessions) {
			sessionArray = sessionCollection.toArray(new InnerSession[0]);
		}
		for (InnerSession session : sessionArray) {
			if (!session.isValid()) {
				this.remove(session);
			}
		}
		*/
		logger.info("[MTPServer] 回收过期会话，剩余数量：" + sessions.size());
		
	}
	
	public Map<Long, InnerSession> getManagerdSessions(){
		return readOnlySessions;
	}

	public void remove(InnerSession session) {
		Map<Long, InnerSession> sessions = this.sessions;
		synchronized (sessions) {
			sessions.remove(session.getSessionID());
			logger.info("session removed >> "+session.getSessionID());
		}
		
		/*
		if (session.destroyImmediately()) {
			Map<Long, InnerSession> sessions = this.sessions;
			synchronized (sessions) {
				sessions.remove(session.getSessionID());
			}
		}
		session.activeDoor();
		*/
//		synchronized (session) {
//			if (!session.isValid()) {
//				session.destroyImmediately();
//				Map<String, InnerSession> sessions = this.sessions;
//				synchronized (sessions) {
//					sessions.remove(session.getSessionID());
//				}
//			}
//		}
		
	}

}
