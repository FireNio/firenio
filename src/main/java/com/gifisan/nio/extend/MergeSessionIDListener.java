package com.gifisan.nio.extend;

import com.gifisan.nio.component.ReadFutureFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.SessionEventListener;
import com.gifisan.nio.component.future.ReadFuture;

public class MergeSessionIDListener implements SessionEventListener{

	public static final String MERGE_SESSION_ID_LISTENER = "_MERGE_SESSION_ID_LISTENER";
	
	public void sessionOpened(Session session) {
		
		ReadFuture future = ReadFutureFactory.create(session, MERGE_SESSION_ID_LISTENER);
		
		future.write(String.valueOf(session.getSessionID()));
		
		session.flush(future);
	}

	public void sessionClosed(Session session) {
		
	}
}
