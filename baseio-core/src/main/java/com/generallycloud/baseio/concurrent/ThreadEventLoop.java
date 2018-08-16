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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.RejectedExecutionHandle;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class ThreadEventLoop extends AbstractEventLoop implements ExecutorEventLoop {

    private static final Logger logger = LoggerFactory.getLogger(ThreadEventLoop.class);
    private ChannelContext         context;
    private ExecutorEventLoopGroup executorEventLoopGroup;
    private RejectedExecutionHandle rejectedExecutionHandle;

    public ThreadEventLoop(ExecutorEventLoopGroup eventLoopGroup, ChannelContext context) {
        this.context = context;
        this.executorEventLoopGroup = eventLoopGroup;
        this.rejectedExecutionHandle = eventLoopGroup.getRejectedExecutionHandle();
    }

    private BlockingQueue<Runnable> jobs;

    @Override
    protected void doLoop() throws InterruptedException {
        Runnable runnable = jobs.poll(32, TimeUnit.MILLISECONDS);
        if (runnable == null) {
            return;
        }
        runnable.run();
    }

    @Override
    protected void doStartup() throws Exception {
        int eventQueueSize = context.getWorkEventQueueSize();
        this.jobs = new ArrayBlockingQueue<>(eventQueueSize);
        super.doStartup();
    }

    @Override
    public void execute(Runnable job) throws RejectedExecutionException {
        if (!jobs.offer(job)) {
            rejectedExecutionHandle.reject(this, job);
            return;
        }
        if (!isRunning() && jobs.remove(job)) {
            rejectedExecutionHandle.reject(this, job);
        }
    }

    @Override
    protected void doStop() {
        for (;;) {
            Runnable runnable = jobs.poll();
            if (runnable == null) {
                break;
            }
            try {
                runnable.run();
            } catch (Throwable e) {
                logger.error(e);
            }
        }
        super.doStop();
    }

    @Override
    public ExecutorEventLoopGroup getGroup() {
        return executorEventLoopGroup;
    }

}
