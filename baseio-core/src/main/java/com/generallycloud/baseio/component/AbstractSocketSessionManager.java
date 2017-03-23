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
package com.generallycloud.baseio.component;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.generallycloud.baseio.OverflowException;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.Logger;
import com.generallycloud.baseio.common.LoggerFactory;
import com.generallycloud.baseio.concurrent.ReentrantMap;

public abstract class AbstractSocketSessionManager extends AbstractSessionManager implements SocketSessionManager{

	protected SocketChannelContext				context			= null;
	protected ConcurrentMap<Integer, SocketSession>	sessions			= new ConcurrentHashMap<>();
	protected Map<Integer, SocketSession>			readOnlySessions	= Collections.unmodifiableMap(sessions);
	protected ReentrantMap<Integer, SocketSession>	iteratorSessions	= new ReentrantMap<>(
			new LinkedHashMap<>());
	private Logger								logger			= LoggerFactory
			.getLogger(getClass());

	public AbstractSocketSessionManager(SocketChannelContext context) {
		super(context.getSessionIdleTime());
		this.context = context;
	}

	@Override
	protected void sessionIdle(long lastIdleTime, long currentTime) {

		Map<Integer, SocketSession> map = iteratorSessions.takeSnapshot();

		if (map.size() == 0) {
			return;
		}

		Collection<SocketSession> es = map.values();

		SocketChannelContext context = this.context;

		for (SocketSession session : es) {

			sessionIdle(context, session, lastIdleTime, currentTime);
		}
	}

	private void sessionIdle(SocketChannelContext context, SocketSession session,
			long lastIdleTime, long currentTime) {

		Linkable<SocketSessionIdleEventListener> linkable = context
				.getSessionIdleEventListenerLink();

		for (; linkable != null;) {

			try {

				linkable.getValue().sessionIdled(session, lastIdleTime, currentTime);

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			linkable = linkable.getNext();
		}
	}

	@Override
	public void stop() {

		Map<Integer, SocketSession> map = iteratorSessions.takeSnapshot();

		if (map.size() == 0) {
			return;
		}

		Collection<SocketSession> es = map.values();

		for (SocketSession session : es) {

			CloseUtil.close(session);
		}
	}

	public void putSession(SocketSession session) throws OverflowException {

		ConcurrentMap<Integer, SocketSession> sessions = this.sessions;

		Integer sessionID = session.getSessionId();

		SocketSession old = sessions.get(sessionID);

		if (old != null) {
			CloseUtil.close(old);
			removeSession(old);
		}

		if (sessions.size() >= getSessionSizeLimit()) {
			throw new OverflowException("session size limit:" + getSessionSizeLimit()
					+ ",current:" + sessions.size());
		}

		sessions.put(sessionID, session);
		iteratorSessions.put(sessionID, session);
	}

	public void removeSession(SocketSession session) {
		sessions.remove(session.getSessionId());
		iteratorSessions.remove(session.getSessionId());
	}

	@Override
	public int getManagedSessionSize() {
		return sessions.size();
	}

	public SocketSession getSession(Integer sessionID) {
		return sessions.get(sessionID);
	}
	
	@Override
	public Map<Integer, SocketSession> getManagedSessions() {
		return readOnlySessions;
	}

	public interface SocketSessionManagerEvent {

		public abstract void fire(SocketChannelContext context,
				Map<Integer, SocketSession> sessions);
	}
}
