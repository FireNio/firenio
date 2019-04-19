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
package test.others;

import java.nio.ByteBuffer;

import com.firenio.buffer.ByteBuf;

/**
 * @author wangkai
 */
public class TestMask {

    public static void main(String[] args) {
        ByteBuf buf  = ByteBuf.wrap("hello word!".getBytes());
        byte    mask = (byte) buf.remaining();
        mask(buf, mask);
        System.out.println(new String(buf.getBytes()));
        buf.position(0);
        mask(buf, mask);
        System.out.println(new String(buf.getBytes()));
    }

    public static void mask(ByteBuf src, byte m) {
        ByteBuffer buf = src.nioBuffer();
        int        p   = buf.position();
        int        l   = buf.limit();
        for (; p < l; p++) {
            buf.put(p, (byte) (buf.get(p) ^ m));
        }
    }

}
