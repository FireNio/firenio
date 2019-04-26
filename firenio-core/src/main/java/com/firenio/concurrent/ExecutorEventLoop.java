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

import com.firenio.common.Util;
import com.firenio.log.Logger;
import com.firenio.log.LoggerFactory;

public final class ExecutorEventLoop extends EventLoop {

    private static final Logger                  logger = LoggerFactory.getLogger(ExecutorEventLoop.class);
    private              ExecutorEventLoopGroup  group;
    private              BlockingQueue<Runnable> jobs;
    private              WorkThread[]            workThreads;

    public ExecutorEventLoop(ExecutorEventLoopGroup group) {
        super(group.getEventLoopName());
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
    protected void doStart() {
        int eventLoopSize = group.getEventLoopSize();
        int maxQueueSize  = group.getMaxQueueSize() * eventLoopSize;
        this.jobs = new ArrayBlockingQueue<>(maxQueueSize);
        this.workThreads = new WorkThread[eventLoopSize];
        for (int i = 0; i < eventLoopSize; i++) {
            workThreads[i] = new WorkThread(jobs);
        }
    }

    @Override
    protected void onStarted() {
        for (int i = 0; i < workThreads.length; i++) {
            String t_name = group.getEventLoopName() + "-" + i;
            Util.exec(workThreads[i], t_name);
        }
    }

    @Override
    protected void doStop() {
        for (int i = 0; i < group.getEventLoopSize(); i++) {
            workThreads[i].stop();
        }
        for (; ; ) {
            Runnable job = jobs.poll();
            if (job == null) {
                break;
            }
            runJob(job);
        }
    }

    @Override
    public ExecutorEventLoopGroup getGroup() {
        return group;
    }

    @Override
    public BlockingQueue<Runnable> getJobs() {
        return jobs;
    }

    @Override
    public Thread getMonitor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean inEventLoop() {
        return false;
    }

    @Override
    public boolean inEventLoop(Thread thread) {
        return false;
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException();
    }

    class WorkThread implements Runnable {

        final BlockingQueue<Runnable> jobs;

        volatile boolean running = true;

        public WorkThread(BlockingQueue<Runnable> jobs) {
            this.jobs = jobs;
        }

        @Override
        public void run() {
            for (; running; ) {
                try {
                    runJob(jobs.poll(1000, TimeUnit.MILLISECONDS));
                } catch (InterruptedException e1) {
                }
            }
        }

        void stop() {
            running = false;
        }

    }

}
