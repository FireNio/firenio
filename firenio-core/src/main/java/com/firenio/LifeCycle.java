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
import java.util.Collections;
import java.util.List;

import com.firenio.common.Assert;
import com.firenio.common.Util;
import com.firenio.log.Logger;
import com.firenio.log.LoggerFactory;

public abstract class LifeCycle {

    private static final byte                    FAILED             = -1;
    private static final Logger                  logger             = LoggerFactory.getLogger(LifeCycle.class);
    private static final byte                    RUNNING            = 2;
    private static final byte                    STARTING           = 1;
    private static final byte                    STOPPED            = 0;
    private static final byte                    STOPPING           = 3;
    private              List<LifeCycleListener> lifeCycleListeners = new ArrayList<>();
    private volatile     byte                    state              = STOPPED;

    public synchronized void addLifeCycleListener(LifeCycleListener listener) {
        checkNotRunning();
        lifeCycleListeners.add(listener);
        Collections.sort(lifeCycleListeners);
    }

    protected void checkNotRunning() {
        Assert.expectFalse(isRunning(), "already running");
    }

    protected abstract void doStart() throws Exception;

    protected abstract void doStop();

    private void fireEvent(int event, Exception exception) {
        if (lifeCycleListeners.size() == 0) {
            return;
        }
        if (event == STARTING) {
            for (LifeCycleListener listener : lifeCycleListeners) {
                try {
                    listener.lifeCycleStarting(this);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } else if (event == RUNNING) {
            for (LifeCycleListener listener : lifeCycleListeners) {
                try {
                    listener.lifeCycleStarted(this);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } else if (event == STOPPING) {
            for (LifeCycleListener listener : lifeCycleListeners) {
                try {
                    listener.lifeCycleStopping(this);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } else if (event == STOPPED) {
            for (LifeCycleListener listener : lifeCycleListeners) {
                try {
                    listener.lifeCycleStopped(this);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } else if (event == FAILED) {
            for (LifeCycleListener listener : lifeCycleListeners) {
                try {
                    listener.lifeCycleFailure(this, exception);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public boolean isFailed() {
        return state == FAILED;
    }

    public boolean isRunning() {
        return state == RUNNING;
    }

    public boolean isStarting() {
        return state == STARTING;
    }

    public boolean isStopped() {
        return state == STOPPED;
    }

    public boolean isStopping() {
        return state == STOPPING;
    }

    public synchronized void removeLifeCycleListener(LifeCycleListener listener) {
        checkNotRunning();
        lifeCycleListeners.remove(listener);
        Collections.sort(lifeCycleListeners);
    }

    public synchronized void start() throws Exception {
        if (!isRunning()) {
            this.state = STARTING;
            this.fireEvent(state, null);
            try {
                this.doStart();
                this.state = RUNNING;
                this.onStarted();
            } catch (Exception e) {
                Util.stop(this);
                this.state = FAILED;
                this.fireEvent(state, e);
                throw e;
            }
            this.fireEvent(state, null);
        }
    }

    protected void onStarted() throws Exception { }

    public synchronized void stop() {
        if (isRunning()) {
            this.state = STOPPING;
            this.fireEvent(state, null);
            try {
                wakeup();
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.doStop();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            this.state = STOPPED;
            this.fireEvent(state, null);
        }
    }

    public void wakeup() {}

}
