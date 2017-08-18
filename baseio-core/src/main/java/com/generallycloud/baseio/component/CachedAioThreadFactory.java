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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class CachedAioThreadFactory implements ThreadFactory {

    private ThreadGroup             group;
    private AtomicInteger           threadNumber = new AtomicInteger(0);
    private String                  namePrefix;
    private AioSocketChannelContext channelContext;

    public CachedAioThreadFactory(AioSocketChannelContext channelContext, String namePrefix) {
        SecurityManager s = System.getSecurityManager();
        this.channelContext = channelContext;
        this.group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.namePrefix = namePrefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        int coreIndex = threadNumber.getAndIncrement();
        String name = namePrefix + "-" + coreIndex;
        Thread t = new CachedAioThread(channelContext, group, r, name, coreIndex);
        if (t.isDaemon()) {
            t.setDaemon(false);
        }
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }

    public boolean inFactory(Thread thread) {
        return thread instanceof SocketChannelThreadContext;
    }

}
