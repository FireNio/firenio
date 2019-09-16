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
package test.io;


import com.firenio.log.LoggerFactory;

/**
 * @author: wangkai
 **/
public class TestByteBufSkip {

    public static void main(String[] args) {

        LoggerFactory.setEnableSLF4JLogger(false);

        test_firenio();
        test_netty();

    }

    static void test_firenio() {
        com.firenio.buffer.ByteBuf out = com.firenio.buffer.ByteBuf.buffer(100);

        int write_index = out.writeIndex();
        out.skipWrite(4);
        out.writeInt(100);
        out.writeInt(200);
        out.setInt(write_index, out.readableBytes() - 4);

        System.out.println("firenio: ");
        System.out.println(out.readInt());
        System.out.println(out.readInt());
        System.out.println(out.readInt());


    }


    static void test_netty() {

        io.netty.buffer.ByteBuf out = io.netty.buffer.Unpooled.buffer();

        int writerIndex = out.writerIndex();
        out.writerIndex(out.writerIndex() + 4);
        out.writeInt(100);
        out.writeInt(200);
        out.setInt(writerIndex, out.readableBytes() - 4);

        System.out.println("netty: ");
        System.out.println(out.readInt());
        System.out.println(out.readInt());
        System.out.println(out.readInt());

    }


}
