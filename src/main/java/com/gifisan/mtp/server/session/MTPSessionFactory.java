package com.gifisan.mtp.server.session;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.component.ServletService;
import com.gifisan.mtp.component.TaskExecutor;
import com.gifisan.mtp.schedule.Job;
import com.gifisan.mtp.server.ServerEndPoint;
import com.gifisan.mtp.server.ServletContext;

public class MTPSessionFactory extends AbstractLifeCycle implements Job {

	private int					lastSize		= 0;
	private final Logger			logger		= LoggerFactory.getLogger(ServletService.class);
	private HashMap<String, Session>	sessions		= new HashMap<String, Session>();
	private TaskExecutor			taskExecutor	= null;

	protected void doStart() throws Exception {
		this.taskExecutor = new TaskExecutor(this, "Session-manager-Task", 30 * 60 * 1000);
		this.taskExecutor.start();
	}

	protected void doStop() throws Exception {
		this.taskExecutor.stop();
	}

	public Session getSession(ServletContext context, ServerEndPoint endPoint, String sessionID) {
		Session session = sessions.get(sessionID);
		if (session == null) {
			synchronized (sessions) {
				session = sessions.get(sessionID);
				if (session == null) {
					session = new MTPSession(context, endPoint, sessionID);
				}
				sessions.put(sessionID, session);
			}
		} else {
			session.active(endPoint);

		}
		return session;
	}

	public void schedule() {
		Map<String, Session> sessions = this.sessions;
		Collection<Session> sessionCollection = sessions.values();
		Session[] sessionArray = sessionCollection.toArray(new Session[0]);
		for (Session session : sessionArray) {
			if (!session.isValid()) {
				synchronized (sessions) {
					sessions.remove(session.getSessionID());
				}
			}
		}
		if (sessions.size() != lastSize) {
			logger.info("[MTPServer] 回收过期会话，剩余数量：" + sessions.size());
		}
	}

}
