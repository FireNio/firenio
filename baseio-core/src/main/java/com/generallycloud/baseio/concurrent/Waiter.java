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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Waiter<T> {

    private ReentrantLock lock     = new ReentrantLock();
    private Condition     callback = lock.newCondition();
    private boolean       isDnoe;
    private boolean       timeouted;
    private T             t;

    /**
     * @param timeout
     * @return timeouted
     */
    public boolean await() {

        ReentrantLock lock = this.lock;

        lock.lock();

        if (isDnoe) {

            lock.unlock();

            return false;
        }

        try {
            callback.await();
        } catch (InterruptedException e) {
            callback.signal();
        }

        timeouted = !isDnoe;

        lock.unlock();

        return timeouted;
    }

    /**
     * @param timeout
     * @return timeouted
     */
    public boolean await(long timeout) {

        ReentrantLock lock = this.lock;

        lock.lock();

        if (isDnoe) {

            lock.unlock();

            return false;
        }

        try {
            callback.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            callback.signal();
        }

        timeouted = !isDnoe;

        lock.unlock();

        return timeouted;
    }

    public void setPayload(T t) {
        ReentrantLock lock = this.lock;

        lock.lock();

        this.isDnoe = true;

        this.t = t;

        callback.signal();

        lock.unlock();
    }

    public boolean isDnoe() {
        return isDnoe;
    }

    public T getPayload() {
        return t;
    }

    public boolean isTimeouted() {
        return timeouted;
    }
}
