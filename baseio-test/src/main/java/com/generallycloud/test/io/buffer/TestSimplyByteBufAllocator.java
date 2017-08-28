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

import java.util.Random;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.buffer.SimplyByteBufAllocator;
import com.generallycloud.baseio.common.ReleaseUtil;
import com.generallycloud.baseio.common.ThreadUtil;

public class TestSimplyByteBufAllocator {

    static ByteBufAllocator allocator = null;

    public static void main(String[] args) throws Exception {

        int capacity = 100;
        int unit = 1;
        int max = unit * capacity / 5;

        allocator = new SimplyByteBufAllocator(capacity, unit, false);
        //		 allocator = new SimpleByteBufAllocator(capacity, unit, false);

        allocator.start();

        Runnable r = () -> {
            for (;;) {

                int random = new Random().nextInt(max);

                if (random == 0) {
                    continue;
                }

                ByteBuf buf = allocator.allocate(random);

                if (buf == null) {
                    System.out.println(buf + Thread.currentThread().getName());
                }

                ThreadUtil.sleep(new Random().nextInt(20));

                ReleaseUtil.release(buf);

                //				String des = allocator.toString();
                //				
                //				if (des.indexOf("free=100") == -1) {
                //					System.out.println();
                //				}

                // ThreadUtil.sleep(10);

                debug();
            }
        };

        ThreadUtil.execute(r);

        ThreadUtil.execute(r);

        //		 ThreadUtil.execute(r);
        //		
        //		 ThreadUtil.execute(r);

    }

    static void debug() {

    }
}
