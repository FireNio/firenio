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

import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.configuration.ServerConfiguration;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class ThreadEventLoop extends AbstractEventLoop implements ExecutorEventLoop {

    private SocketChannelContext   context;

    private static Logger          logger = LoggerFactory.getLogger(ThreadEventLoop.class);

    private ExecutorEventLoopGroup executorEventLoopGroup;

    public ThreadEventLoop(ExecutorEventLoopGroup eventLoopGroup, SocketChannelContext context) {
        this.executorEventLoopGroup = eventLoopGroup;
        this.context = context;
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

        ServerConfiguration sc = context.getServerConfiguration();

        int eventQueueSize = sc.getSERVER_WORK_EVENT_QUEUE_SIZE();

        this.jobs = new ArrayBlockingQueue<>(eventQueueSize);

        super.doStartup();
    }

    //FIXME 观察这里是否部分event没有被fire
    @Override
    public void dispatch(Runnable job) throws RejectedExecutionException {
        if (!isRunning() || !jobs.offer(job)) {
            throw new RejectedExecutionException();
        }
    }

    @Override
    protected void doStop() {

        boolean sleeped = false;

        for (;;) {

            Runnable runnable = jobs.poll();

            if (runnable == null) {
                if (sleeped) {
                    break;
                }
                ThreadUtil.sleep(8);
                sleeped = true;
                continue;
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
    public ExecutorEventLoopGroup getEventLoopGroup() {
        return executorEventLoopGroup;
    }

}
