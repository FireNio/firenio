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

import com.firenio.baseio.LifeCycleUtil;
import com.firenio.baseio.component.FastThreadLocalThread;
import com.firenio.baseio.log.Logger;
import com.firenio.baseio.log.LoggerFactory;

public abstract class EventLoop implements Runnable {

    private static final Logger   logger       = LoggerFactory.getLogger(EventLoop.class);
    private EventLoopGroup        defaultGroup = new DefaultEventLoopGroup(this);
    private FastThreadLocalThread monitor      = null;
    private volatile boolean      running      = false;

    public final void assertInEventLoop() {
        assertInEventLoop("this operation must eval in event loop");
    }

    public final void assertInEventLoop(String msg) {
        if (!inEventLoop()) {
            throw new RuntimeException(msg);
        }
    }

    protected void doLoop() throws Exception {}

    protected void doStartup() throws Exception {}

    protected void doStop() {}

    public EventLoopGroup getGroup() {
        return defaultGroup;
    }

    public abstract BlockingQueue<Runnable> getJobs();

    public int getMaxQueueSize() {
        return getGroup().getMaxQueueSize();
    }

    public Thread getMonitor() {
        return monitor;
    }

    public boolean inEventLoop() {
        return inEventLoop(Thread.currentThread());
    }

    public boolean inEventLoop(Thread thread) {
        return getMonitor() == thread;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void run() {
        for (;;) {
            if (!running) {
                try {
                    doStop();
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
                return;
            }
            try {
                doLoop();
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void startup(String threadName) throws Exception {
        synchronized (this) {
            if (running) {
                return;
            }
            this.running = true;
            this.monitor = new FastThreadLocalThread(this, threadName);
            this.doStartup();
            EventLoopListener listener = getGroup().getEventLoopListener();
            if (listener != null) {
                listener.onStartup(this);
            }
            this.monitor.start();
        }
    }

    public void stop() {
        synchronized (this) {
            if (!running) {
                return;
            }
            running = false;
            try {
                wakeup();
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
            //            if (!inEventLoop()) {
            //                for (; !isStopped();) {
            //                    Util.sleep(4);
            //                }
            //            }
            EventLoopListener l = getGroup().getEventLoopListener();
            if (l != null) {
                try {
                    l.onStop(this);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public void wakeup() {}

    class DefaultEventLoopGroup extends EventLoopGroup {

        private EventLoop eventLoop;

        DefaultEventLoopGroup(EventLoop eventLoop) {
            super("");
            this.eventLoop = eventLoop;
        }

        @Override
        protected void doStart() throws Exception {}

        @Override
        protected void doStop() {
            LifeCycleUtil.stop(eventLoop);
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
