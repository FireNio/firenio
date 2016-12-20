/*
 * Copyright 2015 GenerallyCloud.com
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
package com.generallycloud.nio.component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;

import com.generallycloud.nio.Linkable;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.concurrent.ListQueue;
import com.generallycloud.nio.component.concurrent.ListQueueABQ;
import com.generallycloud.nio.component.concurrent.ReentrantMap;

//所有涉及操作全部session的操作放在此队列中做
public class DatagramSessionManagerImpl extends AbstractSessionManager implements DatagramSessionManager {

	private DatagramChannelContext						context	= null;
	private ReentrantMap<InetSocketAddress, DatagramSession>	sessions	= new ReentrantMap<InetSocketAddress, DatagramSession>();
	private ListQueue<DatagramSessionManagerEvent>			events	= new ListQueueABQ<DatagramSessionManagerEvent>(
			512);
	private Logger										logger	= LoggerFactory
			.getLogger(DatagramSessionManagerImpl.class);

	public DatagramSessionManagerImpl(DatagramChannelContext context) {
		super(context.getSessionIdleTime());
		this.context = context;
	}

	@Override
	public void offerSessionMEvent(DatagramSessionManagerEvent event) {
		// FIXME throw
		this.events.offer(event);
	}

	@Override
	protected void fireSessionManagerEvent() {

		DatagramSessionManagerEvent event = events.poll();

		Map<InetSocketAddress, DatagramSession> map = sessions.getSnapshot();

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
	}

	@Override
	protected void sessionIdle(long lastIdleTime, long currentTime) {

		Map<InetSocketAddress, DatagramSession> map = sessions.getSnapshot();

		if (map.size() == 0) {
			return;
		}

		Collection<DatagramSession> es = map.values();

		DatagramChannelContext context = this.context;

		for (DatagramSession session : es) {

			sessionIdle(context, session, lastIdleTime, currentTime);
		}

	}

	protected void sessionIdle(DatagramChannelContext context, DatagramSession session, long lastIdleTime,
			long currentTime) {

		Linkable<DatagramSessionEventListener> linkable = context.getSessionEventListenerLink();

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
	public void close() throws IOException {

		Map<InetSocketAddress, DatagramSession> map = sessions.getSnapshot();

		if (map.size() == 0) {
			return;
		}

		Collection<DatagramSession> es = map.values();

		for (DatagramSession session : es) {

			CloseUtil.close(session);
		}
	}

	@Override
	public void putSession(DatagramSession session) {

		ReentrantMap<InetSocketAddress, DatagramSession> sessions = this.sessions;

		InetSocketAddress remote = session.getRemoteSocketAddress();

		DatagramSession old = sessions.get(remote);

		if (old != null) {
			CloseUtil.close(old);
			removeSession(old);
		}

		sessions.put(remote, session);
	}

	@Override
	public void removeSession(DatagramSession session) {
		sessions.remove(session.getRemoteSocketAddress());
	}

	@Override
	public int getManagedSessionSize() {
		return sessions.size();
	}

	@Override
	public DatagramSession getSession(InetSocketAddress sessionID) {
		return sessions.get(sessionID);
	}

	@Override
	public DatagramSession getSession(DatagramChannelSelectorLoop selectorLoop,
			java.nio.channels.DatagramChannel nioChannel, InetSocketAddress remote) throws IOException {

		DatagramSession session = sessions.get(remote);

		if (session == null) {

			@SuppressWarnings("resource")
			DatagramChannel channel = new NioDatagramChannel(selectorLoop, nioChannel, remote);

			session = channel.getSession();

			putSession(session);
		}

		return session;
	}

}
