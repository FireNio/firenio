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

    private boolean                       failed                  = false;
    private List<LifeCycleListener>       lifeCycleListeners      = new ArrayList<>();
    private Logger                        logger                  = LoggerFactory
            .getLogger(AbstractLifeCycle.class);
    private boolean                       running                 = false;
    private boolean                       starting                = false;
    private boolean                       stopped                 = true;
    private boolean                       stopping                = false;
    private Comparator<LifeCycleListener> lifeCycleListenerSorter = new Comparator<LifeCycleListener>() {

                                                                      @Override
                                                                      public int compare(
                                                                              LifeCycleListener o1,
                                                                              LifeCycleListener o2) {

                                                                          return o1
                                                                                  .lifeCycleListenerSortIndex() > o2
                                                                                          .lifeCycleListenerSortIndex()
                                                                                                  ? 1
                                                                                                  : -1;
                                                                      }
                                                                  };

    @Override
    public void addLifeCycleListener(LifeCycleListener listener) {
        synchronized (lifeCycleListeners) {
            lifeCycleListeners.add(listener);
            Collections.sort(lifeCycleListeners, lifeCycleListenerSorter);
        }
    }

    protected abstract void doStart() throws Exception;

    protected abstract void doStop() throws Exception;

    protected boolean logger() {
        return true;
    }

    private void fireEvent(int event) {
        if (lifeCycleListeners.size() == 0) {
            return;
        }
        switch (event) {
            case STARTING:
                synchronized (lifeCycleListeners) {
                    for (LifeCycleListener listener : lifeCycleListeners) {
                        try {
                            listener.lifeCycleStarting(this);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
                break;
            case RUNNING:
                synchronized (lifeCycleListeners) {
                    for (LifeCycleListener listener : lifeCycleListeners) {
                        try {
                            listener.lifeCycleStarted(this);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
                break;
            case STOPPING:
                synchronized (lifeCycleListeners) {
                    for (LifeCycleListener listener : lifeCycleListeners) {
                        try {
                            listener.lifeCycleStopping(this);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
                break;
            case STOPPED:
                synchronized (lifeCycleListeners) {
                    for (LifeCycleListener listener : lifeCycleListeners) {
                        try {
                            listener.lifeCycleStopped(this);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    private void fireFailed(Exception exception) {
        if (lifeCycleListeners.size() == 0) {
            return;
        }
        synchronized (lifeCycleListeners) {
            for (LifeCycleListener listener : lifeCycleListeners) {
                try {
                    listener.lifeCycleFailure(this, exception);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public boolean isFailed() {
        return this.failed;
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    @Override
    public boolean isStarted() {
        return this.running;
    }

    @Override
    public boolean isStarting() {
        return this.starting;
    }

    @Override
    public boolean isStopped() {
        return this.stopped;
    }

    @Override
    public boolean isStopping() {
        return this.stopping;
    }

    @Override
    public void removeLifeCycleListener(LifeCycleListener listener) {
        synchronized (lifeCycleListeners) {
            lifeCycleListeners.remove(listener);
            Collections.sort(lifeCycleListeners, lifeCycleListenerSorter);
        }
    }

    @Override
    public void start() throws Exception {

        if (this.stopped != true && this.stopping != true) {
            throw new IllegalStateException("did not stopped");
        }

        this.starting = true;

        this.fireEvent(STARTING);

        try {

            this.doStart();

            if (logger()) {
                LoggerUtil.prettyLog(logger, "loaded [ {} ]", this.toString());
            }

        } catch (Exception e) {

            this.failed = true;

            this.starting = false;

            this.fireFailed(e);

            throw e;
        }

        this.starting = false;

        this.running = true;

        this.stopped = false;

        this.stopping = false;

        this.fireEvent(RUNNING);

    }

    @Override
    public void stop() {

        //		if (this.starting != true && this.running != true) {
        //			throw new IllegalStateException("stopped,"+this.toString());
        //		}

        this.running = false;

        this.stopping = true;

        this.fireEvent(STOPPING);

        try {

            this.doStop();

            if (logger()) {
                LoggerUtil.prettyLog(logger, "unloaded [ {} ]", this.toString());
            }

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

        }

        this.stopping = false;

        this.stopped = true;

        this.fireEvent(STOPPED);

    }

    public void softStart() {

        this.starting = false;

        this.running = true;

        this.stopped = false;

        this.stopping = false;

    }

}
