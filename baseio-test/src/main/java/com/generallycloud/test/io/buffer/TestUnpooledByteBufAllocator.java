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
package com.generallycloud.test.io.buffer;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.baseio.common.ReleaseUtil;

/**
 * @author wangkai
 *
 */
public class TestUnpooledByteBufAllocator {

    public static void main(String[] args) {
        test();
    }

    static void test() {

        UnpooledByteBufAllocator allocator = UnpooledByteBufAllocator.getDirectInstance();

        byte[] data = "你好啊，world".getBytes();

        ByteBuf buf = allocator.allocate(data.length);

        buf.put(data);

        buf.flip();

        ReleaseUtil.release(buf);

        for (;;) {

            ByteBuf buf2 = buf.duplicate();

            byte[] bb = buf2.getBytes();

            String s = new String(bb);

            System.out.println(s);
        }

    }
}
