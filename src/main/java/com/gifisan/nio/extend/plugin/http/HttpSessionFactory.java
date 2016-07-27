package com.gifisan.nio.extend.plugin.http;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.concurrent.ReentrantMap;
import com.gifisan.nio.component.protocol.http11.future.Cookie;
import com.gifisan.nio.component.protocol.http11.future.HttpReadFuture;

public class HttpSessionFactory implements Runnable{
	
	private static final String COOKIE_NAME_SESSIONID = "NSESSIONID";

	private ReentrantMap<String, HttpSession> sessions = new ReentrantMap<String, HttpSession>();
	
	public void putSession(String sessionID,HttpSession session){
		sessions.put(sessionID, session);
	}
	
	public void removeSession(String sessionID){
		sessions.remove(sessionID);
	}
	
	public HttpSession getHttpSession(HttpContext context,Session ioSession, HttpReadFuture future){
		
		String sessionID = future.getCookie(COOKIE_NAME_SESSIONID);
		
		if (StringUtil.isNullOrBlank(sessionID)) {
			
			DefaultHttpSession session = new DefaultHttpSession(context,ioSession);
			
			sessionID = session.getSessionID();
			
			Cookie cookie = new Cookie(COOKIE_NAME_SESSIONID, sessionID);
			
			future.addCookie(cookie);
			
			this.sessions.put(sessionID, session);
			
			return session;
		}
		
		HttpSession session = sessions.get(sessionID);
		
		if (session == null) {
			
			session = new DefaultHttpSession(context,ioSession, sessionID);
			
			this.sessions.put(sessionID, session);
		}
		
		session.active(ioSession);
		
		return session;
	}
	
	public void run(){
		
		ReentrantLock lock = sessions.getReentrantLock();
		
		lock.lock();
		
		try{
			
			Map<String, HttpSession> map = sessions.getSnapshot();
			
			Set<Entry<String, HttpSession>> es = map.entrySet();
			
			for(Entry<String, HttpSession> e :es){
				HttpSession session = e.getValue();
				
				if (!session.isValidate()) {
					sessions.remove(e.getKey());
				}
			}
			
		}finally{
			
			lock.unlock();
		}
	}
}
