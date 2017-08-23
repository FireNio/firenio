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
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;

import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.Future;
import com.generallycloud.baseio.protocol.ProtocolEncoder;

public class AioSocketSessionManager extends AbstractSessionManager
        implements SocketSessionManager {

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
            removeSession(old);
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
        if (getManagedSessionSize() == 0) {
            return;
        }
        ChannelFuture f = (ChannelFuture) future;
        ProtocolEncoder encoder = context.getProtocolEncoder();
        ByteBufAllocator allocator = UnpooledByteBufAllocator.getHeapInstance();
        encoder.encode(allocator, f);
        broadcastChannelFuture(f);
    }

    @Override
    public void broadcastChannelFuture(ChannelFuture future) {
        if (getManagedSessionSize() == 0) {
            return;
        }
        for (SocketSession s : sessions.values()) {
            s.doFlush(future.duplicate());
        }
    }

    @Override
    public Map<Integer, SocketSession> getManagedSessions() {
        return readOnlySessions;
    }

}
