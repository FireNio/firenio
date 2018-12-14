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

public class ExecutorPoolEventLoop implements ExecutorEventLoop {

    private NamedThreadFactory     threadFactory;
    private ExecutorEventLoopGroup group;

    public ExecutorPoolEventLoop(ExecutorEventLoopGroup group) {
        this.group = group;
    }

    private ThreadPoolExecutor poolExecutor;

    @Override
    public void startup(String threadName) throws Exception {
        int eventLoopSize = group.getEventLoopSize();
        int maxQueueSize = group.getMaxQueueSize();
        threadFactory = new NamedThreadFactory(threadName);
        poolExecutor = new ThreadPoolExecutor(eventLoopSize, eventLoopSize, Long.MAX_VALUE,
                TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(maxQueueSize),
                threadFactory);
    }

    @Override
    public void stop() {
        if (poolExecutor != null) {
            poolExecutor.shutdown();
        }
    }

    @Override
    public BlockingQueue<Runnable> getJobs() {
        return poolExecutor.getQueue();
    }

    @Override
    public int getEventSize() {
        return poolExecutor.getQueue().size();
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
        return group;
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
