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
package test.io.buffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import com.firenio.baseio.buffer.ByteBuf;
import com.firenio.baseio.buffer.PooledByteBufAllocator;
import com.firenio.baseio.buffer.PooledByteBufAllocator.PoolState;
import com.firenio.baseio.common.Util;

import junit.framework.Assert;

/**
 * @author wangkai
 *
 */
public class TestPooledBytebuf {

    @Test
    public void testAlloc() throws Exception {
        final PooledByteBufAllocator a = TestAllocUtil.heap();
        CountDownLatch c = new CountDownLatch(Util.availableProcessors());
        for (int i = 0; i < Util.availableProcessors(); i++) {
            Util.exec(() -> {
                Random r = new Random();
                List<ByteBuf> bufs = new ArrayList<>();
                int alloc = 0;
                for (int j = 0; j < 99999; j++) {
                    int pa = r.nextInt((a.getCapacity() - alloc) / 2);
                    if (pa == 0) {
                        pa = 1;
                    }
                    ByteBuf buf = a.allocate(pa);
                    a.toString();
                    bufs.add(buf);
                    alloc += buf.limit();
                    if (buf.limit() % 3 != 0) {
                        buf.release();
                        alloc -= buf.limit();
                        bufs.remove(buf);
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
        Assert.assertTrue(s.buf == 0);
        Assert.assertTrue(s.free == s.memory);
        Assert.assertTrue(s.mfree == s.memory);
    }

}
