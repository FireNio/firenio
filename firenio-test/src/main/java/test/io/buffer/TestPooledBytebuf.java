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
package test.io.buffer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;
import org.junit.Test;

import com.firenio.DevelopConfig;
import com.firenio.buffer.ByteBuf;
import com.firenio.buffer.PooledByteBufAllocator;
import com.firenio.buffer.PooledByteBufAllocator.PoolState;
import com.firenio.common.Util;
import com.firenio.log.LoggerFactory;

/**
 * @author wangkai
 */
public class TestPooledBytebuf {

    @Test
    public void testAlloc() throws Exception {
        DevelopConfig.BUF_DEBUG = true;
        LoggerFactory.setEnableSLF4JLogger(false);
        final AtomicInteger          alloc_single_times   = new AtomicInteger();
        final AtomicInteger          alloc_unpooled_times = new AtomicInteger();
        final int                    core                 = Math.max(4, Util.availableProcessors());
        final PooledByteBufAllocator a                    = TestAllocUtil.heap(1024 * 1024 * 4 * core);
        CountDownLatch               c                    = new CountDownLatch(core);
        for (int i = 0; i < core; i++) {
            Util.exec(() -> {
                Random         r    = new Random();
                Queue<ByteBuf> bufs = new LinkedList<>();
                for (int j = 0; j < 1024 * 1024; j++) {
                    int pa = r.nextInt(256);
                    if (pa == 0) {
                        pa = 1;
                        alloc_single_times.getAndIncrement();
                    }
                    ByteBuf buf = a.allocate(pa);
                    if (!buf.isPooled()) {
                        alloc_unpooled_times.getAndIncrement();
                    }
                    if ((pa & 1) == 1) {
                        buf.release();
                    } else {
                        bufs.offer(buf);
                    }
                    if (bufs.size() > 256) {
                        ByteBuf buf1 = bufs.poll();
                        buf1.release();
                    }
                }
                for (ByteBuf buf : bufs) {
                    buf.release();
                }
                c.countDown();
            });
        }
        c.await();
        PoolState s = a.getState();
        System.out.println("alloc_single_times: " + alloc_single_times.get());
        System.out.println("alloc_unpooled_times: " + alloc_unpooled_times.get());
        Assert.assertEquals(0, s.buf);
        Assert.assertEquals(s.free, s.memory);
        Assert.assertEquals(s.mfree, s.memory);
    }

}
