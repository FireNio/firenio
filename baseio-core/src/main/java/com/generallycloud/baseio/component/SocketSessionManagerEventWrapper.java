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

import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.baseio.collection.IntObjectHashMap;

/**
 * @author wangkai
 *
 */
public class SocketSessionManagerEventWrapper implements SocketSessionManagerEvent {

    private SocketSessionManagerEvent event;

    private AtomicInteger             countor;

    public SocketSessionManagerEventWrapper(int core, SocketSessionManagerEvent event) {
        super();
        this.event = event;
        this.countor = new AtomicInteger(core);
    }

    @Override
    public void fire(SocketChannelContext context, IntObjectHashMap<SocketSession> sessions) {
        try {
            unwrap().fire(context, sessions);
        }finally{
            complated(context, sessions);
        }
    }

    @Override
    public void complated(SocketChannelContext context, IntObjectHashMap<SocketSession> sessions) {
        if (countor.decrementAndGet() == 0) {
            unwrap().complated(context, sessions);
        }
    }
    
    private SocketSessionManagerEvent unwrap(){
        return event;
    }

}
