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
package com.generallycloud.test.others;

/**
 * @author wangkai
 *
 */
public class VolatileExample {

    int          x = 0;
    volatile int b = 0;

    private void write() {
        x = 5;
        b = 1;
    }

    @SuppressWarnings("unused")
    private void read() {
        int dummy = b;
        while (x != 5) {}
    }

    public static void main(String[] args) throws Exception {
        final VolatileExample example = new VolatileExample();
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                example.write();
            }
        });
        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                example.read();
            }
        });
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
    }
}
