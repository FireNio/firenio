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

import com.firenio.buffer.ByteBuf;
import com.firenio.buffer.ByteBufAllocator;

import junit.framework.Assert;

/**
 * @author wangkai
 */
public class TestExpansion {

    static final byte   a    = 'a';
    static final byte[] data = "aaaa".getBytes();

    private static boolean v(ByteBuf buf) {
        buf.flip();
        String s = new String(buf.getBytes());
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
            buf.putBytes(data);
            buf.putByte(a);
            alloc.allocate(1);
        }
        Assert.assertTrue(v(buf));
    }

    @Test
    public void arrayUnPool() {
        ByteBuf buf = ByteBuf.heap(2);
        for (int i = 0; i < 10; i++) {
            buf.putBytes(data);
            buf.putByte(a);
        }
        Assert.assertTrue(v(buf));
    }

    @Test
    public void directPool() throws Exception {
        ByteBufAllocator alloc = TestAllocUtil.direct(1024 * 4);
        ByteBuf          buf   = alloc.allocate(2);
        for (int i = 0; i < 10; i++) {
            buf.putBytes(data);
            buf.putByte(a);
            alloc.allocate(1);
        }
        Assert.assertTrue(v(buf));
    }

    @Test
    public void directUnPool() {
        ByteBuf buf = ByteBuf.direct(2);
        for (int i = 0; i < 10; i++) {
            buf.putBytes(data);
            buf.putByte(a);
        }
        Assert.assertTrue(v(buf));
    }

}
