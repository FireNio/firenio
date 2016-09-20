package com.generallycloud.nio.component;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.concurrent.ListQueue;
import com.generallycloud.nio.component.concurrent.ListQueueABQ;
import com.generallycloud.nio.component.concurrent.ReentrantMap;

//所有涉及操作全部session的操作放在此队列中做
public class SessionFactory extends AbstractLooper {

	private NIOContext					context;
	private long						next_idle_time	= System.currentTimeMillis();
	private long						current_idle_time;
	private long						last_idle_time ;
	private ReentrantMap<Integer, Session>	sessions		= new ReentrantMap<Integer, Session>();
	private ListQueue<SessionMEvent>		events		= new ListQueueABQ<SessionMEvent>(512);
	private Logger						logger		= LoggerFactory.getLogger(SessionFactory.class);

	protected SessionFactory(NIOContext context) {
		this.context = context;
	}

	public void putSession(Session session) {

		sessions.put(session.getSessionID(), session);
	}

	public void loop() {

		SessionMEvent event = this.events.poll(16);

		if (event != null) {
			event.handle(sessions.getSnapshot());
		}

		long current_time = System.currentTimeMillis();

		if (next_idle_time > current_time) {
			return;
		}
		
		this.last_idle_time = this.current_idle_time;
		
		this.current_idle_time = current_time;
		
		this.next_idle_time = current_idle_time + context.getSessionIdleTime(); 

		Map<Integer, Session> map = this.sessions.getSnapshot();

		if (map.size() == 0) {
			return;
		}

		Set<Entry<Integer, Session>> es = map.entrySet();

		for (Entry<Integer, Session> e : es) {

			Session s = e.getValue();

			sessionIdle(s, last_idle_time, current_time);
		}
	}

	// FIXME 优化这个方法
	private void sessionIdle(Session session, long lastIdleTime, long currentTime) {

		SessionEventListenerWrapper listenerWrapper = context.getSessionEventListenerStub();

		for (; listenerWrapper != null;) {
			try {
				listenerWrapper.sessionIdled(session, lastIdleTime, currentTime);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			listenerWrapper = listenerWrapper.nextListener();
		}
	}

	public Session getSession(Integer sessionID) {

		return sessions.get(sessionID);
	}

	public void removeSession(Session session) {

		sessions.remove(session.getSessionID());
	}

	public void offerSessionMEvent(SessionMEvent event) {
		// throw
		this.events.offer(event);
	}
	
	public int getManagedSessionSize(){
		return sessions.size();
	}

}
