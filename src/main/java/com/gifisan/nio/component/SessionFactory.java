package com.gifisan.nio.component;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.gifisan.nio.component.concurrent.ReentrantList;
import com.gifisan.nio.component.concurrent.ReentrantMap;

//所有涉及操作全部session的操作放在此队列中做
public class SessionFactory extends AbstractLooper{
	
	private ReentrantMap<Integer, Session>	sessions	= new ReentrantMap<Integer, Session>();

	private ReentrantList<SessionMEvent>	events	= new ReentrantList<SessionMEvent>();
	
	public void putSession(Session session) {

		sessions.put(session.getSessionID(), session);
	}
	
	private void fireEvents(List<SessionMEvent> events){
		
		for(SessionMEvent e : events){
			
			e.handle(sessions.getSnapshot());
		}
		
		events.clear();
	}

	public void loop() {
		
		List<SessionMEvent> events = this.events.getSnapshot();
		
		if (!events.isEmpty()) {

			fireEvents(events);
		}

		Map<Integer, Session> map = this.sessions.getSnapshot();
		
		if (map.size() == 0) {
			
			sleep(60 * 60 * 1000);
			
			return;
		}
		
		Set<Entry<Integer, Session>> es = map.entrySet();
		
		long limit = System.currentTimeMillis() - 60 * 60 * 1000; 
		
		for(Entry<Integer,Session> e :es){
			
			Session s = e.getValue();
			
			if (s.getLastAccessTime() > limit ) {
				continue;
			}
			
			s.destroy();
		}
		
		sleep(60 * 60 * 1000);
	}

	public Session getSession(Integer sessionID) {

		return sessions.get(sessionID);
	}

	public void removeSession(Session session) {

		sessions.remove(session.getSessionID());
	}
	
	public void offerSessionMEvent(SessionMEvent event){
		this.events.add(event);
	}

}
