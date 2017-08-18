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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.baseio.balance.facade.BalanceFacadeSocketSession;
import com.generallycloud.baseio.balance.reverse.BalanceReverseSocketSession;
import com.generallycloud.baseio.protocol.Future;

public class SimpleNextRouter extends AbstractBalanceRouter {

    private int                               index      = 0;
    private ReentrantLock                     lock       = new ReentrantLock();
    private List<BalanceReverseSocketSession> routerList = new ArrayList<>();

    private BalanceReverseSocketSession getNextRouterSession() {

        List<BalanceReverseSocketSession> list = this.routerList;

        if (list.isEmpty()) {
            return null;
        }

        BalanceReverseSocketSession session;

        if (index < list.size()) {

            session = list.get(index++);
        } else {

            index = 1;

            session = list.get(0);
        }

        return session;
    }

    @Override
    public void addRouterSession(BalanceReverseSocketSession session) {

        ReentrantLock lock = this.lock;

        lock.lock();

        this.routerList.add(session);

        lock.unlock();
    }

    @Override
    public void removeRouterSession(BalanceReverseSocketSession session) {

        ReentrantLock lock = this.lock;

        lock.lock();

        routerList.remove(session);

        lock.unlock();
    }

    @Override
    public BalanceReverseSocketSession getRouterSession(BalanceFacadeSocketSession session,
            Future future) {

        BalanceReverseSocketSession router_session = getRouterSession(session);

        if (router_session == null) {

            return getRouterSessionFresh(session);
        }

        if (router_session.isClosed()) {

            return getRouterSessionFresh(session);
        }

        return router_session;
    }

    private BalanceReverseSocketSession getRouterSessionFresh(BalanceFacadeSocketSession session) {

        ReentrantLock lock = this.lock;

        lock.lock();

        try {

            BalanceReverseSocketSession router_session = getRouterSession(session);

            if (router_session == null || router_session.isClosed()) {

                router_session = getNextRouterSession();

                if (router_session == null) {
                    return null;
                }

                session.setReverseSocketSession(router_session);
            }

            return router_session;

        } finally {

            lock.unlock();
        }
    }

}
