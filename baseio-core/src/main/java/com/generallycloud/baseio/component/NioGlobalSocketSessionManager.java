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
import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.Future;
import com.generallycloud.baseio.protocol.ProtocolEncoder;

/**
 * @author wangkai
 *
 */
public class NioGlobalSocketSessionManager implements SocketSessionManager {

    private Map<Integer, SocketSession> sessions         = new ConcurrentHashMap<>();

    private Map<Integer, SocketSession> readOnlySessions = Collections.unmodifiableMap(sessions);

    private SocketSessionManager[]      socketSessionManagers;

    private int                         managerLen;

    private NioSocketChannelContext     context;

    public void init(NioSocketChannelContext context) {
        this.context = context;
        NioChannelService service = (NioChannelService) context.getChannelService();
        SocketSelectorEventLoopGroup group = service.getSelectorEventLoopGroup();
        SocketSelectorEventLoop[] loops = group.getSelectorEventLoops();
        socketSessionManagers = new SocketSessionManager[loops.length];
        managerLen = loops.length;
        for (int i = 0; i < managerLen; i++) {
            socketSessionManagers[i] = loops[i].getSocketSessionManager();
        }
    }

    private AtomicInteger managedSessionSize = new AtomicInteger();

    @Override
    public int getManagedSessionSize() {
        return managedSessionSize.get();
    }

    @Override
    public SocketSession getSession(int sessionId) {
        return socketSessionManagers[sessionId % managerLen].getSession(sessionId);
    }

    @Override
    public void loop() {
        for (SocketSessionManager m : socketSessionManagers) {
            m.loop();
        }
    }

    @Override
    public void stop() {
        for (SocketSessionManager m : socketSessionManagers) {
            m.stop();
        }
    }

    @Override
    public void putSession(SocketSession session) {
        sessions.put(session.getSessionId(), session);
        managedSessionSize.incrementAndGet();
    }

    @Override
    public void removeSession(SocketSession session) {
        if (sessions.remove(session.getSessionId()) != null) {
            managedSessionSize.decrementAndGet();
        }
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
