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
package com.generallycloud.baseio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

//FIXME status volatile modify ?
public abstract class AbstractLifeCycle implements LifeCycle {

    private List<LifeCycleListener> lifeCycleListeners = new ArrayList<>();
    private static final Logger     logger             = LoggerFactory
            .getLogger(AbstractLifeCycle.class);
    private int                     state              = STOPPED;

    @Override
    public void addLifeCycleListener(LifeCycleListener listener) {
        synchronized (lifeCycleListeners) {
            lifeCycleListeners.add(listener);
            Collections.sort(lifeCycleListeners, new LifeCycleListenerSorter());
        }
    }
    
    protected void checkNotRunning() {
        if (isRunning()) {
            throw new UnsupportedOperationException("already running");
        }
    }

    protected abstract void doStart() throws Exception;

    protected abstract void doStop() throws Exception;

    private void fireEvent(int event, Exception exception) {
        if (lifeCycleListeners.size() == 0) {
            return;
        }
        synchronized (lifeCycleListeners) {
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
    }

    @Override
    public boolean isFailed() {
        return state == FAILED;
    }

    @Override
    public boolean isRunning() {
        return state == RUNNING;
    }

    @Override
    public boolean isStarting() {
        return state == STARTING;
    }

    @Override
    public boolean isStopped() {
        return state == STOPPED;
    }

    @Override
    public boolean isStopping() {
        return state == STOPPING;
    }

    protected boolean logger() {
        return true;
    }

    @Override
    public void removeLifeCycleListener(LifeCycleListener listener) {
        synchronized (lifeCycleListeners) {
            lifeCycleListeners.remove(listener);
            Collections.sort(lifeCycleListeners, new LifeCycleListenerSorter());
        }
    }

    @Override
    public void start() throws Exception {
        if (isStopping() || isRunning()) {
            throw new IllegalStateException("did not stopped");
        }
        this.state = STARTING;
        this.fireEvent(state, null);
        try {
            this.doStart();
            if (logger()) {
                LoggerUtil.prettyLog(logger, "loaded [ {} ]", this.toString());
            }
        } catch (Exception e) {
            this.state = FAILED;
            this.fireEvent(state, e);
            throw e;
        }
        this.state = RUNNING;
        this.fireEvent(state, null);
    }

    @Override
    public void stop() {
        if (isStopping() || isStopped() || isFailed()) {
            return;
        }
        this.state = STOPPING;
        this.fireEvent(state, null);
        try {
            this.doStop();
            if (logger()) {
                LoggerUtil.prettyLog(logger, "unloaded [ {} ]", this.toString());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        this.state = STOPPED;
        this.fireEvent(state, null);
    }

    class LifeCycleListenerSorter implements Comparator<LifeCycleListener> {

        @Override
        public int compare(LifeCycleListener o1, LifeCycleListener o2) {
            return o1.lifeCycleListenerSortIndex() > o2.lifeCycleListenerSortIndex() ? 1 : -1;
        }
    };

}
