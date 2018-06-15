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

import com.generallycloud.baseio.balance.facade.FacadeSocketChannel;
import com.generallycloud.baseio.balance.reverse.ReverseSocketChannel;
import com.generallycloud.baseio.protocol.Future;

public class SimpleNextRouter extends AbstractBalanceRouter {

    private int                        index      = 0;
    private ReentrantLock              lock       = new ReentrantLock();
    private List<ReverseSocketChannel> routerList = new ArrayList<>();

    private ReverseSocketChannel getNextRouterChannel() {
        List<ReverseSocketChannel> list = this.routerList;
        if (list.isEmpty()) {
            return null;
        }
        ReverseSocketChannel channel;
        if (index < list.size()) {
            channel = list.get(index++);
        } else {
            index = 1;
            channel = list.get(0);
        }
        return channel;
    }

    @Override
    public void addRouterChannel(ReverseSocketChannel channel) {
        ReentrantLock lock = this.lock;
        lock.lock();
        this.routerList.add(channel);
        lock.unlock();
    }

    @Override
    public void removeRouterChannel(ReverseSocketChannel channel) {
        ReentrantLock lock = this.lock;
        lock.lock();
        routerList.remove(channel);
        lock.unlock();
    }

    @Override
    public ReverseSocketChannel getRouterChannel(FacadeSocketChannel channel, Future future) {
        ReverseSocketChannel router_session = getRouterChannel(channel);
        if (router_session == null) {
            return getRouterChannelFresh(channel);
        }
        if (router_session.isClosed()) {
            return getRouterChannelFresh(channel);
        }
        return router_session;
    }

    private ReverseSocketChannel getRouterChannelFresh(FacadeSocketChannel channel) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            ReverseSocketChannel router_session = getRouterChannel(channel);
            if (router_session == null || router_session.isClosed()) {
                router_session = getNextRouterChannel();
                if (router_session == null) {
                    return null;
                }
                channel.setReverseSocketChannel(router_session);
            }
            return router_session;
        } finally {
            lock.unlock();
        }
    }

}
