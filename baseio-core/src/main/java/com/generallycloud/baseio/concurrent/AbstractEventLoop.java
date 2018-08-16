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

import com.generallycloud.baseio.AbstractLifeCycle;
import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.common.Assert;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.FastThreadLocalThread;
import com.generallycloud.baseio.component.RejectedExecutionHandle;
import com.generallycloud.baseio.component.RejectedExecutionHandle.DefaultRejectedExecutionHandle;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public abstract class AbstractEventLoop implements EventLoop {

    private static final Logger   logger       = LoggerFactory.getLogger(AbstractEventLoop.class);
    private EventLoopGroup        defaultGroup = new DefaultEventLoopGroup(this);
    private FastThreadLocalThread monitor      = null;
    private volatile boolean      running      = false;
    private volatile boolean      stopped      = false;

    @Override
    public void execute(Runnable event) {}

    protected void doLoop() throws Exception {}

    protected void doStartup() throws Exception {}

    protected void doStop() {}

    @Override
    public EventLoopGroup getGroup() {
        return defaultGroup;
    }

    @Override
    public FastThreadLocalThread getMonitor() {
        return monitor;
    }

    @Override
    public boolean inEventLoop() {
        return inEventLoop(Thread.currentThread());
    }

    @Override
    public boolean inEventLoop(Thread thread) {
        return getMonitor() == thread;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private boolean isStopped() {
        return stopped;
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
                stopped = true;
                return;
            }
            try {
                doLoop();
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    protected void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    @Override
    public void startup(String threadName) throws Exception {
        synchronized (this) {
            if (running) {
                return;
            }
            this.running = true;
            this.stopped = false;
            this.monitor = new FastThreadLocalThread(this, threadName);
            this.doStartup();
            LoggerUtil.prettyLog(logger, "loaded [ {} ]", this);
            EventLoopListener listener = getGroup().getEventLoopListener();
            if (listener != null) {
                listener.onStartup(this);
            }
            this.monitor.start();
        }
    }

    @Override
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
            for (; !isStopped();) {
                ThreadUtil.sleep(4);
            }
            LoggerUtil.prettyLog(logger, "event looper {} stopped", this);
            try {
                EventLoopListener listener = getGroup().getEventLoopListener();
                if (listener != null) {
                    listener.onStop(this);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void wakeup() {}

    class DefaultEventLoopGroup extends AbstractLifeCycle implements EventLoopGroup {

        private EventLoop               eventLoop;
        private EventLoopListener       listener;
        private RejectedExecutionHandle rejectedExecutionHandle = new DefaultRejectedExecutionHandle();

        DefaultEventLoopGroup(EventLoop eventLoop) {
            this.eventLoop = eventLoop;
        }

        @Override
        protected void doStart() throws Exception {}

        @Override
        protected void doStop() throws Exception {
            LifeCycleUtil.stop(eventLoop);
        }

        @Override
        public EventLoop getEventLoop(int index) {
            return eventLoop;
        }

        @Override
        public EventLoopListener getEventLoopListener() {
            return listener;
        }

        @Override
        public EventLoop getNext() {
            return eventLoop;
        }

        @Override
        public RejectedExecutionHandle getRejectedExecutionHandle() {
            return rejectedExecutionHandle;
        }

        @Override
        public void setEventLoopListener(EventLoopListener listener) {
            this.listener = listener;
        }

        public void setRejectedExecutionHandle(RejectedExecutionHandle rejectedExecutionHandle) {
            Assert.notNull(rejectedExecutionHandle, "null rejectedExecutionHandle");
            this.rejectedExecutionHandle = rejectedExecutionHandle;
        }

    }

}
