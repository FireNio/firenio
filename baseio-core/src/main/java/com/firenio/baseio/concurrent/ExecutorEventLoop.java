/*
 * Copyright 2015 The Baseio Project
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
package com.firenio.baseio.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.firenio.baseio.component.FastThreadLocalThread;

public final class ExecutorEventLoop extends EventLoop {

    private ExecutorEventLoopGroup group;
    private ThreadPoolExecutor     poolExecutor;
    private NamedThreadFactory     threadFactory;

    public ExecutorEventLoop(ExecutorEventLoopGroup group) {
        this.group = group;
    }

    @Override
    public ExecutorEventLoopGroup getGroup() {
        return group;
    }

    @Override
    public BlockingQueue<Runnable> getJobs() {
        return poolExecutor.getQueue();
    }

    @Override
    public Thread getMonitor() {
        throw new UnsupportedOperationException();
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
    public boolean isRunning() {
        return !poolExecutor.isShutdown();
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void startup(String threadName) throws Exception {
        EventLoopListener l = group.getEventLoopListener();
        if (l != null) {
            try {
                l.onStartup(this);
            } catch (Exception e) {
            }
        }
        int eventLoopSize = group.getEventLoopSize();
        int maxQueueSize = group.getMaxQueueSize();
        threadFactory = new NamedThreadFactory(threadName);
        poolExecutor = new ThreadPoolExecutor(eventLoopSize, eventLoopSize, Long.MAX_VALUE,
                TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(maxQueueSize),
                threadFactory);
    }

    @Override
    public void stop() {
        EventLoopListener l = group.getEventLoopListener();
        if (l != null) {
            try {
                l.onStop(this);
            } catch (Exception e) {
            }
        }
        if (poolExecutor != null) {
            poolExecutor.shutdown();
        }
    }

    @Override
    public void wakeup() {
        throw new UnsupportedOperationException();
    }

    class NamedThreadFactory implements ThreadFactory {

        private final ThreadGroup   group;
        private final String        namePrefix;
        private final AtomicInteger threadNumber = new AtomicInteger(1);

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
