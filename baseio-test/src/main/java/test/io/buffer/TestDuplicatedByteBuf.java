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

import org.junit.Test;

import com.firenio.baseio.buffer.ByteBuf;
import com.firenio.baseio.buffer.ByteBufAllocator;

import junit.framework.Assert;

/**
 * @author wangkai
 *
 */
public class TestDuplicatedByteBuf {

    static final String data = "abcdef";

    @Test
    public void testDirect() throws Exception {
        ByteBuf buf = ByteBuf.direct(16);

        buf.putBytes(data.getBytes());
        buf.flip();
        ByteBuf buf2 = buf.duplicate();
        v(buf2);
    }

    @Test
    public void testDirectP() throws Exception {
        ByteBufAllocator a = TestAllocUtil.direct();
        ByteBuf buf = a.allocate(16);

        buf.putBytes(data.getBytes());
        buf.flip();
        ByteBuf buf2 = buf.duplicate();
        v(buf2);
    }

    @Test
    public void testHeap() throws Exception {
        ByteBuf buf = ByteBuf.direct(16);

        buf.putBytes(data.getBytes());
        buf.flip();
        ByteBuf buf2 = buf.duplicate();
        v(buf2);
    }

    @Test
    public void testHeapP() throws Exception {
        ByteBufAllocator a = TestAllocUtil.heap();
        ByteBuf buf = a.allocate(16);

        buf.putBytes(data.getBytes());
        buf.flip();
        ByteBuf buf2 = buf.duplicate();
        v(buf2);
    }

    static void v(ByteBuf buf) {

        Assert.assertTrue(new String(buf.getBytes()).equals(data));

    }

}
