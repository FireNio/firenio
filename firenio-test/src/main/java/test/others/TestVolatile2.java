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

/**
 * @author: wangkai
 **/
public class TestVolatile2 {

    static          boolean running = true;
    static volatile int     mem_bar = 0;

    public static void main(String[] args) throws Exception {
        new Thread(() -> {
            int temp = 0;
            for (; running; ) {
                temp = mem_bar;
            }
            System.out.println("worker stop");
        }).start();
        Thread.sleep(500);
        running = false;
        System.out.println("set running false");
    }

}
