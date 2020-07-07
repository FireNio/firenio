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

import org.junit.Test;

import com.firenio.Options;
import com.firenio.buffer.ByteBuf;
import com.firenio.buffer.ByteBufAllocator;
import com.firenio.buffer.ByteBufAllocatorGroup;
import com.firenio.buffer.PooledByteBufAllocator;
import com.firenio.common.Unsafe;

import junit.framework.Assert;

/**
 * @author wangkai
 */
public class TestExpansion {

    static final byte   a    = 'a';
    static final byte[] data = "aaaa".getBytes();

    private static boolean v(ByteBuf buf) {
        String s = new String(buf.readBytes());
        if (s.length() != 50) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != 'a') {
                return false;
            }
        }
        return true;
    }

    @Test
    public void arrayPool() throws Exception {
        ByteBufAllocator alloc = TestAllocUtil.heap(1024 * 4);
        ByteBuf          buf   = alloc.allocate(2);
        for (int i = 0; i < 10; i++) {
            buf.writeBytes(data);
            buf.writeByte(a);
            alloc.allocate(1);
        }
        Assert.assertTrue(v(buf));
    }

    @Test
    public void arrayUnPool() {
        ByteBuf buf = ByteBuf.heap(2);
        buf.clear();
        for (int i = 0; i < 10; i++) {
            buf.writeBytes(data);
            buf.writeByte(a);
        }
        Assert.assertTrue(v(buf));
    }

    @Test
    public void directPool() throws Exception {
        ByteBufAllocator alloc = TestAllocUtil.direct(1024 * 4);
        ByteBuf          buf   = alloc.allocate(2);
        for (int i = 0; i < 10; i++) {
            buf.writeBytes(data);
            buf.writeByte(a);
            alloc.allocate(1);
        }
        Assert.assertTrue(v(buf));
    }

    @Test
    public void directUnPool() {
        ByteBuf buf = ByteBuf.direct(2);
        for (int i = 0; i < 10; i++) {
            buf.writeBytes(data);
            buf.writeByte(a);
        }
        Assert.assertTrue(v(buf));
    }

    @Test
    public void testExpansion() throws Exception {
        Options.setBufAutoExpansion(true);
        ByteBufAllocatorGroup g = new ByteBufAllocatorGroup(1, 8, 1, Unsafe.BUF_DIRECT);
        g.start();
        PooledByteBufAllocator alloc = g.getAllocator(0);
        ByteBuf                buf1  = alloc.allocate(2);
        buf1.expansion(6);
        buf1.release();
        ByteBuf buf2 = alloc.allocate(2);
        buf2.expansion(4);
        buf2.release();
        Assert.assertTrue(alloc.getState().buf == 0);
        g.stop();
    }

}
