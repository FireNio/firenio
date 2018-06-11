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
package com.generallycloud.baseio.balance.router;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.generallycloud.baseio.balance.facade.FacadeSocketSession;
import com.generallycloud.baseio.balance.reverse.ReverseSocketSession;

public abstract class AbstractBalanceRouter implements BalanceRouter {

    private ConcurrentMap<Object, FacadeSocketSession> clients = new ConcurrentHashMap<>();

    @Override
    public void addClientSession(FacadeSocketSession session) {
        this.clients.put(session.getSessionKey(), session);
    }

    @Override
    public FacadeSocketSession getClientSession(Object key) {
        return clients.get(key);
    }

    @Override
    public void removeClientSession(FacadeSocketSession session) {
        this.clients.remove(session.getSessionKey());
    }

    @Override
    public ReverseSocketSession getRouterSession(FacadeSocketSession session) {
        return session.getReverseSocketSession();
    }
}
