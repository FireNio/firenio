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
import java.util.concurrent.TimeUnit;

import com.firenio.baseio.log.Logger;
import com.firenio.baseio.log.LoggerFactory;

public final class ThreadEventLoop extends EventLoop {

    private static final Logger     logger = LoggerFactory.getLogger(ThreadEventLoop.class);
    private ThreadEventLoopGroup    group;

    private BlockingQueue<Runnable> jobs;

    public ThreadEventLoop(ThreadEventLoopGroup group) {
        this.group = group;
    }

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
        int maxQueueSize = group.getMaxQueueSize();
        this.jobs = new ArrayBlockingQueue<>(maxQueueSize);
        super.doStartup();
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
    public ThreadEventLoopGroup getGroup() {
        return group;
    }

    @Override
    public BlockingQueue<Runnable> getJobs() {
        return jobs;
    }

    public boolean offer(Runnable runnable) {
        return jobs.offer(runnable);
    }

}
