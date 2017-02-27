/*
 * Copyright 2015-2017 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package com.generallycloud.nio.codec.http11;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.codec.http11.future.Cookie;
import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.DebugUtil;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.component.concurrent.AbstractEventLoop;
import com.generallycloud.nio.component.concurrent.ReentrantMap;

//FIXME 限制最大session数量
public class HttpSessionManager extends AbstractEventLoop{
	
	private static final String COOKIE_NAME_SESSIONID = "BSESSIONID";

	private ReentrantMap<String, HttpSession> sessions = new ReentrantMap<String, HttpSession>();
	
	public void putSession(String sessionID,HttpSession session){
		sessions.put(sessionID, session);
	}
	
	public void removeSession(String sessionID){
		sessions.remove(sessionID);
	}
	
	public HttpSession getHttpSession(HttpContext context,SocketSession ioSession, HttpReadFuture future){
		
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
	
	@Override
	public void doLoop(){
		
		ReentrantLock lock = sessions.getReentrantLock();
		
		lock.lock();
		
		try{
			
			Map<String, HttpSession> map = sessions.takeSnapshot();
			
			Collection<HttpSession> es = map.values();
			
			for(HttpSession session :es){
				
				if (!session.isValidate()) {
					sessions.remove(session.getSessionID());
					CloseUtil.close(session.getIoSession());
				}
			}
			
		}finally{
			
			lock.unlock();
		}
		
		sleep(30 * 60 * 1000);
	}
	
	private void sleep(long time){
		synchronized (this) {
			try {
				this.wait(time);
			} catch (InterruptedException e) {
				DebugUtil.debug(e);
			}
		}
	}
	
	@Override
	public void wakeup() {
		synchronized (this) {
			this.notify();
		}
		super.wakeup();
	}

	public int getManagedSessionSize(){
		return sessions.size();
	}

}
