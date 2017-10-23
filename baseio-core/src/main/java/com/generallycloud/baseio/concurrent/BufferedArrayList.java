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
package com.generallycloud.baseio.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class BufferedArrayList<T> {

    private List<T>       list1  = new ArrayList<>();
    private List<T>       list2  = new ArrayList<>();
    private ReentrantLock lock   = new ReentrantLock();
    private List<T>       buffer = list1;

    public List<T> getBuffer() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (buffer == list1) {
                buffer = list2;
                buffer.clear();
                return list1;
            } else {
                buffer = list1;
                buffer.clear();
                return list2;
            }
        } finally {
            lock.unlock();
        }
    }

    public int getBufferSize() {
        return list1.size() + list2.size();
    }

    public ReentrantLock getReentrantLock() {
        return lock;
    }

    public void offer(T t) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            buffer.add(t);
        } finally {
            lock.unlock();
        }
    }

}
