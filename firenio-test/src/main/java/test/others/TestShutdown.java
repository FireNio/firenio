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

import com.firenio.common.Unsafe;
import com.firenio.log.Logger;
import com.firenio.log.LoggerFactory;

/**
 * @author: wangkai
 **/
public class TestShutdown {


    public static void main(String[] args) {
        LoggerFactory.setEnableSLF4JLogger(false);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("shutdown....");
        }));
        System.out.println("start..");
        long address = Unsafe.allocate(1);
        Unsafe.free(address);

        Unsafe.putInt(address,100);
        System.out.println("put address success..");

        ByteBuffer buf = ByteBuffer.allocateDirect(100);
        Unsafe.freeByteBuffer(buf);
        buf.putLong(100);
        System.out.println("put buffer success..");

        Unsafe.putInt(-1,100);
        System.out.println("put address -1 success..");

        Unsafe.putInt(0,100);
        System.out.println("put address 0 success..");

        System.out.println("end....");

    }




}
