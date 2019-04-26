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
package com.firenio.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.firenio.log.Logger;
import com.firenio.log.LoggerFactory;

public final class ThreadEventLoop extends EventLoop {

    private static final Logger                  logger = LoggerFactory.getLogger(ThreadEventLoop.class);
    private              ThreadEventLoopGroup    group;
    private              BlockingQueue<Runnable> jobs;

    public ThreadEventLoop(ThreadEventLoopGroup group, String threadName) {
        super(threadName);
        this.group = group;
    }

    private static void runJob(Runnable job) {
        if (job != null) {
            try {
                job.run();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    protected void doLoop() throws InterruptedException {
        runJob(jobs.poll(1000, TimeUnit.MILLISECONDS));
    }

    @Override
    protected void doStart() throws Exception {
        int maxQueueSize = group.getMaxQueueSize();
        this.jobs = new ArrayBlockingQueue<>(maxQueueSize);
    }

    @Override
    protected void doStop() {
        for (; ; ) {
            Runnable job = jobs.poll();
            if (job == null) {
                break;
            }
            runJob(job);
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
