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
package com.generallycloud.baseio.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.baseio.component.FastThreadLocalThread;
import com.generallycloud.baseio.component.RejectedExecutionHandle;

public class ExecutorPoolEventLoop implements ExecutorEventLoop {

    private int                    coreEventLoopSize;
    private int                    maxEventLoopSize;
    private long                   keepAliveTime;
    private int                    maxEventQueueSize;
    private NamedThreadFactory     threadFactory;
    private ExecutorEventLoopGroup eventLoopGroup;

    public ExecutorPoolEventLoop(ExecutorEventLoopGroup eventLoopGroup, int coreEventLoopSize,
            int maxEventLoopSize, int maxEventQueueSize, long keepAliveTime) {
        this.eventLoopGroup = eventLoopGroup;
        this.coreEventLoopSize = coreEventLoopSize;
        this.maxEventLoopSize = maxEventLoopSize;
        this.maxEventQueueSize = maxEventQueueSize;
        this.keepAliveTime = keepAliveTime;
    }

    private ThreadPoolExecutor poolExecutor;

    @Override
    public void execute(Runnable job) {
        this.poolExecutor.execute(job);
    }

    @Override
    public void startup(String threadName) throws Exception {
        threadFactory = new NamedThreadFactory(threadName);
        poolExecutor = new ThreadPoolExecutor(coreEventLoopSize, maxEventLoopSize, keepAliveTime,
                TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(maxEventQueueSize),
                threadFactory);
        final EventLoop eventLoop = this;
        final RejectedExecutionHandle handle = eventLoopGroup.getRejectedExecutionHandle();
        poolExecutor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                handle.reject(eventLoop, r);
            }
        });
    }

    @Override
    public void stop() {
        if (poolExecutor != null) {
            poolExecutor.shutdown();
        }
    }

    @Override
    public boolean inEventLoop() {
        return false;
    }

    @Override
    public boolean inEventLoop(Thread thread) {
        return false;
    }

    @Override
    public Thread getMonitor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRunning() {
        return !poolExecutor.isShutdown();
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void wakeup() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExecutorEventLoopGroup getGroup() {
        return eventLoopGroup;
    }

    class NamedThreadFactory implements ThreadFactory {

        private final ThreadGroup   group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String        namePrefix;

        public NamedThreadFactory(String namePrefix) {
            SecurityManager s = System.getSecurityManager();
            this.group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            String threadName = namePrefix + "-" + threadNumber.getAndIncrement();
            Thread t = new FastThreadLocalThread(group, r, threadName, 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }

    }

}
