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

import java.util.concurrent.BlockingQueue;

import com.firenio.baseio.LifeCycle;
import com.firenio.baseio.common.Util;
import com.firenio.baseio.component.FastThreadLocalThread;
import com.firenio.baseio.log.Logger;
import com.firenio.baseio.log.LoggerFactory;

public abstract class EventLoop extends LifeCycle implements Runnable {

    private static final Logger   logger       = LoggerFactory.getLogger(EventLoop.class);
    private final EventLoopGroup  defaultGroup = new DefaultEventLoopGroup(this);
    private FastThreadLocalThread monitor      = null;
    private final String          threadName;

    public EventLoop(String threadName) {
        this.threadName = threadName;
    }

    public final void assertInEventLoop() {
        assertInEventLoop("this operation must eval in event loop");
    }

    public final void assertInEventLoop(String msg) {
        if (!inEventLoop()) {
            throw new RuntimeException(msg);
        }
    }

    protected void doLoop() throws Exception {}

    @Override
    protected void doStart() throws Exception {
        this.monitor = new FastThreadLocalThread(this, threadName);
        this.monitor.start();
    }

    @Override
    protected void doStop() {}

    public EventLoopGroup getGroup() {
        return defaultGroup;
    }

    protected abstract BlockingQueue<Runnable> getJobs();

    public int getMaxQueueSize() {
        return getGroup().getMaxQueueSize();
    }

    public Thread getMonitor() {
        return monitor;
    }

    public int getPendingSize() {
        return getJobs().size();
    }

    public boolean inEventLoop() {
        return inEventLoop(Thread.currentThread());
    }

    public boolean inEventLoop(Thread thread) {
        return getMonitor() == thread;
    }

    @Override
    public void run() {
        for (; isRunning();) {
            try {
                doLoop();
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public boolean submit(Runnable job) {
        final BlockingQueue<Runnable> jobs = getJobs();
        if (!jobs.offer(job)) {
            return false;
        }
        return !(!isRunning() && jobs.remove(job));
    }

    static class DefaultEventLoopGroup extends EventLoopGroup {

        private EventLoop eventLoop;

        DefaultEventLoopGroup(EventLoop eventLoop) {
            super("");
            this.eventLoop = eventLoop;
        }

        @Override
        protected void doStart() throws Exception {}

        @Override
        protected void doStop() {
            Util.stop(eventLoop);
        }

        @Override
        public EventLoop getEventLoop(int index) {
            return eventLoop;
        }

        @Override
        public EventLoop getNext() {
            return eventLoop;
        }

    }

}
