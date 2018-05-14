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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;

import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.Future;

public class AioSocketSessionManager extends AbstractSessionManager {

    private Logger                      logger           = LoggerFactory.getLogger(getClass());
    private SocketChannelContext        context          = null;
    private Map<Integer, SocketSession> sessions         = new ConcurrentHashMap<>();
    private Map<Integer, SocketSession> readOnlySessions = Collections.unmodifiableMap(sessions);

    public AioSocketSessionManager(SocketChannelContext context) {
        super(context.getSessionIdleTime());
        this.context = context;
    }

    @Override
    protected void sessionIdle(long lastIdleTime, long currentTime) {
        Map<Integer, SocketSession> sessions = this.sessions;
        if (sessions.size() == 0) {
            return;
        }
        List<SocketSessionIdleEventListener> ls = context.getSessionIdleEventListeners();
        for (SocketSessionIdleEventListener l : ls) {
            for (SocketSession session : sessions.values()) {
                try {
                    l.sessionIdled(session, lastIdleTime, currentTime);
                } catch (Exception e) {
                    logger.error(e.getMessage(),e);
                }
            }
        }
    }

    @Override
    public void stop() {
        Map<Integer, SocketSession> sessions = this.sessions;
        if (sessions.size() == 0) {
            return;
        }
        for (SocketSession session : sessions.values()) {
            CloseUtil.close(session);
        }
    }

    @Override
    public void putSession(SocketSession session) throws RejectedExecutionException {
        Map<Integer, SocketSession> sessions = this.sessions;
        int sessionId = session.getSessionId();
        SocketSession old = sessions.get(sessionId);
        if (old != null) {
            CloseUtil.close(old);
        }
        if (sessions.size() >= getSessionSizeLimit()) {
            throw new RejectedExecutionException(
                    "session size limit:" + getSessionSizeLimit() + ",current:" + sessions.size());
        }
        sessions.put(sessionId, session);
    }

    @Override
    public void removeSession(SocketSession session) {
        sessions.remove(session.getSessionId());
    }

    @Override
    public int getManagedSessionSize() {
        return sessions.size();
    }

    @Override
    public SocketSession getSession(int sessionId) {
        return sessions.get(sessionId);
    }

    @Override
    public void broadcast(Future future) throws IOException {
        broadcast(future, sessions.values());
    }

    @Override
    public void broadcastChannelFuture(ChannelFuture future) {
        broadcastChannelFuture(future, sessions.values());
    }

    @Override
    public void broadcast(Future future, Collection<SocketSession> sessions) throws IOException {
        if (getManagedSessionSize() == 0) {
            return;
        }
        SocketChannel channel = context.getSimulateSocketChannel();
        ChannelFuture f = (ChannelFuture) future;
        context.getProtocolCodec().encode(channel, f);
        broadcastChannelFuture(f, sessions);
    }

    @Override
    public void broadcastChannelFuture(ChannelFuture future, Collection<SocketSession> sessions) {
        if (sessions.size() == 0) {
            return;
        }
        for (SocketSession s : sessions) {
            s.flushChannelFuture(future.duplicate());
        }
    }

    public Map<Integer, SocketSession> getManagedSessionsMap() {
        return readOnlySessions;
    }

    @Override
    public Map<Integer,SocketSession> getManagedSessions() {
        return readOnlySessions;
    }

}
