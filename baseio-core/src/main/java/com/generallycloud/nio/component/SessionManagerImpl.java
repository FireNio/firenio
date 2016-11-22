package com.generallycloud.nio.component;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.generallycloud.nio.Linkable;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.concurrent.ListQueue;
import com.generallycloud.nio.component.concurrent.ListQueueABQ;
import com.generallycloud.nio.component.concurrent.ReentrantMap;

//所有涉及操作全部session的操作放在此队列中做
public class SessionManagerImpl implements SessionManager {

	private BaseContext					context			= null;
	private long						current_idle_time	= 0;
	private long						last_idle_time		= 0;
	private long						next_idle_time		= System.currentTimeMillis();
	private ReentrantMap<Integer, Session>	sessions			= new ReentrantMap<Integer, Session>();
	private ListQueue<SessionMEvent>		events			= new ListQueueABQ<SessionMEvent>(512);
	private Logger						logger			= LoggerFactory.getLogger(SessionManagerImpl.class);

	public SessionManagerImpl(BaseContext context) {
		this.context = context;
	}

	public void putSession(Session session) {

		Integer sessionID = session.getSessionID();

		Session old = sessions.get(sessionID);

		if (old != null) {
			CloseUtil.close(old);
			removeSession(old);
		}

		sessions.put(sessionID, session);
	}

	public void loop() {

		SessionMEvent event = events.poll();

		Map<Integer, Session> map = sessions.getSnapshot();

		if (map.size() == 0) {
			return;
		}

		if (event != null) {
			try {
				event.fire(context, map);
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			}
		}

		long current_time = System.currentTimeMillis();

		if (next_idle_time > current_time) {
			return;
		}

		this.last_idle_time = this.current_idle_time;

		this.current_idle_time = current_time;

		this.next_idle_time = current_idle_time + context.getSessionIdleTime();

		Set<Entry<Integer, Session>> es = map.entrySet();

		for (Entry<Integer, Session> e : es) {

			Session s = e.getValue();

			sessionIdle(context, s, last_idle_time, current_time);
		}
	}

	// FIXME 优化这个方法
	private void sessionIdle(BaseContext context, Session session, long lastIdleTime, long currentTime) {

		Linkable<SessionEventListener> linkable = context.getSessionEventListenerLink();

		for (; linkable != null;) {

			try {

				linkable.getValue().sessionIdled(session, lastIdleTime, currentTime);

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			linkable = linkable.getNext();
		}
	}

	public Session getSession(Integer sessionID) {

		return sessions.get(sessionID);
	}

	public void removeSession(Session session) {

		sessions.remove(session.getSessionID());
	}

	public void offerSessionMEvent(SessionMEvent event) {
		// FIXME throw
		this.events.offer(event);
	}

	public int getManagedSessionSize() {
		return sessions.size();
	}

	public void close() throws IOException {
		
		Map<Integer, Session> map = sessions.getSnapshot();

		if (map.size() == 0) {
			return;
		}
		
		Collection<Session> es = map.values();
		
		for(Session session : es){
			
			CloseUtil.close(session);
		}
	}

}
