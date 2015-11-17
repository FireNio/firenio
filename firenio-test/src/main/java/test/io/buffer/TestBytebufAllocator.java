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

import junit.framework.Assert;

public class TestBytebufAllocator {

    static final String data  = "hello;hello;";
    static final String data1 = "hello;";

    @Test
    public void testput0() {
        ByteBuf src = ByteBuf.direct(1024);
        src.putBytes(data.getBytes());
        src.flip();

        ByteBuf dst = ByteBuf.direct(1024);
        dst.putBytes(src);
        dst.flip();
        v(dst);
    }

    @Test
    public void testput1() {

        ByteBuf src = ByteBuf.direct(1024);
        src.putBytes(data.getBytes());
        src.flip();

        ByteBuf dst = ByteBuf.direct(data1.length());
        dst.putBytes(src);
        dst.flip();
        v(dst);
    }

    @Test
    public void testput2() {
        ByteBuf src = ByteBuf.heap(1024);
        src.putBytes(data.getBytes());
        src.flip();

        ByteBuf dst = ByteBuf.direct(1024);
        dst.putBytes(src);
        dst.flip();
        v(dst);
    }

    @Test
    public void testPut3() {
        ByteBuf src = ByteBuf.heap(1024);
        src.putBytes(data.getBytes());
        src.flip();

        ByteBuf dst = ByteBuf.direct(data1.length());
        dst.putBytes(src);
        dst.flip();
        v(dst);
    }

    @Test
    public void testPut4() {
        ByteBuf src = ByteBuf.heap(1024);
        src.putBytes("hello;hello;".getBytes());
        src.flip();

        ByteBuf dst = ByteBuf.heap(1024);
        dst.putBytes(src);
        dst.flip();
        v(dst);
    }

    @Test
    public void testPut5() {
        ByteBuf src = ByteBuf.heap(1024);
        src.putBytes("hello;hello;".getBytes());
        src.flip();

        ByteBuf dst = ByteBuf.heap(data1.length());
        dst.putBytes(src);
        dst.flip();
        v(dst);
    }

    @Test
    public void testPut6() {
        ByteBuf src = ByteBuf.direct(1024);
        src.putBytes("hello;hello;".getBytes());
        src.flip();

        ByteBuf dst = ByteBuf.heap(1024);
        dst.putBytes(src);
        dst.flip();
        v(dst);
    }

    @Test
    public void testPut7() {
        ByteBuf src = ByteBuf.direct(1024);
        src.putBytes("hello;hello;".getBytes());
        src.flip();

        ByteBuf dst = ByteBuf.heap(data1.length());
        dst.putBytes(src);
        dst.flip();
        v(dst);
    }

    static void v(ByteBuf buf) {
        byte[] res = buf.getBytes();
        Assert.assertTrue(new String(res).equals(data));
    }

}
