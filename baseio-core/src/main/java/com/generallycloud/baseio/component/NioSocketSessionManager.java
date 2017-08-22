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

import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import com.generallycloud.baseio.collection.IntObjectHashMap;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.Future;

public class NioSocketSessionManager extends AbstractSessionManager
        implements SocketSessionManager {

    private SocketChannelContext            context  = null;
    private SocketSessionManager            parent   = null;
    private IntObjectHashMap<SocketSession> sessions = new IntObjectHashMap<>();

    public NioSocketSessionManager(SocketChannelContext context) {
        super(context.getSessionIdleTime());
        this.context = context;
        this.parent = context.getSessionManager();
    }

    @Override
    protected void sessionIdle(long lastIdleTime, long currentTime) {
        IntObjectHashMap<SocketSession> sessions = this.sessions;
        if (sessions.size() == 0) {
            return;
        }
        SocketChannelContext context = this.context;
        SocketSessionIdleEventListenerWrapper linkable = context.getSessionIdleEventListenerLink();
        if (linkable == null) {
            return;
        }
        for (SocketSession session : sessions.values()) {
            linkable.sessionIdled(session, lastIdleTime, currentTime);
        }
    }

    @Override
    public void stop() {
        IntObjectHashMap<SocketSession> sessions = this.sessions;
        if (sessions.size() == 0) {
            return;
        }
        for (SocketSession session : sessions.values()) {
            CloseUtil.close(session);
        }
    }

    @Override
    public void putSession(SocketSession session) throws RejectedExecutionException {
        IntObjectHashMap<SocketSession> sessions = this.sessions;
        int sessionId = session.getSessionId();
        SocketSession old = sessions.get(sessionId);
        if (old != null) {
            CloseUtil.close(old);
            removeSession(old);
        }
        if (sessions.size() >= getSessionSizeLimit()) {
            throw new RejectedExecutionException(
                    "session size limit:" + getSessionSizeLimit() + ",current:" + sessions.size());
        }
        sessions.put(sessionId, session);
        parent.putSession(session);
    }

    @Override
    public void removeSession(SocketSession session) {
        sessions.remove(session.getSessionId());
        parent.removeSession(session);
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
    public void broadcast(Future future) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void broadcastChannelFuture(ChannelFuture future) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Integer, SocketSession> getManagedSessions() {
        throw new UnsupportedOperationException();
    }

}
