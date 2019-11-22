/*
 * Copyright 2015 The FireNio Project
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
package com.firenio;

import java.util.ArrayList;
import java.util.List;

import com.firenio.common.Assert;
import com.firenio.common.Util;
import com.firenio.log.Logger;
import com.firenio.log.LoggerFactory;

public abstract class LifeCycle {

    private static final Logger logger         = LoggerFactory.getLogger(LifeCycle.class);
    private static final int    EVENT_STOPPED  = 0;
    private static final int    EVENT_STARTING = 1;
    private static final int    EVENT_RUNNING  = 2;
    private static final int    EVENT_STOPPING = 3;
    private static final int    EVENT_FAILED   = -1;

    private volatile boolean                 running            = false;
    private          List<LifeCycleListener> lifeCycleListeners = null;

    public synchronized void addLifeCycleListener(LifeCycleListener listener) {
        checkNotRunning();
        if (lifeCycleListeners == null) {
            lifeCycleListeners = new ArrayList<>();
        }
        lifeCycleListeners.add(listener);
    }

    protected void checkNotRunning() {
        Assert.expectFalse(isRunning(), "already running");
    }

    protected abstract void doStart() throws Exception;

    protected abstract void doStop();

    private void fireEvent(int event, Throwable exception) {
        if (lifeCycleListeners == null) {
            return;
        }
        if (event == EVENT_STARTING) {
            for (LifeCycleListener listener : lifeCycleListeners) {
                try {
                    listener.lifeCycleStarting(this);
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } else if (event == EVENT_RUNNING) {
            for (LifeCycleListener listener : lifeCycleListeners) {
                try {
                    listener.lifeCycleStarted(this);
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } else if (event == EVENT_STOPPING) {
            for (LifeCycleListener listener : lifeCycleListeners) {
                try {
                    listener.lifeCycleStopping(this);
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } else if (event == EVENT_STOPPED) {
            for (LifeCycleListener listener : lifeCycleListeners) {
                try {
                    listener.lifeCycleStopped(this);
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } else if (event == EVENT_FAILED) {
            for (LifeCycleListener listener : lifeCycleListeners) {
                try {
                    listener.lifeCycleFailure(this, exception);
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    public synchronized void removeLifeCycleListener(LifeCycleListener listener) {
        checkNotRunning();
        if (lifeCycleListeners != null) {
            lifeCycleListeners.remove(listener);
        }
    }

    public synchronized void start() throws Exception {
        if (!isRunning()) {
            this.fireEvent(EVENT_STARTING, null);
            try {
                this.doStart();
                this.running = true;
                this.onStarted();
            } catch (Throwable e) {
                Util.stop(this);
                this.running = false;
                this.fireEvent(EVENT_FAILED, e);
                throw e;
            }
            this.fireEvent(EVENT_RUNNING, null);
        }
    }

    protected void onStarted() throws Exception { }

    public synchronized void stop() {
        if (isRunning()) {
            this.fireEvent(EVENT_STOPPING, null);
            try {
                wakeup();
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.doStop();
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
            this.running = false;
            this.fireEvent(EVENT_STOPPED, null);
        }
    }

    public void wakeup() {}

}
