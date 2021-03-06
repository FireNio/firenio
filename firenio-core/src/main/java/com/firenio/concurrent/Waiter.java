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
package com.firenio.concurrent;

import java.io.Closeable;

import com.firenio.common.Util;

public final class Waiter<T> implements Callback<T> {

    private       boolean   isDone;
    private       T         response;
    private       Throwable throwable;
    private       boolean   timeout;
    private final Object    lock;

    public Waiter() {
        this(null);
    }

    public Waiter(Object lock) {
        if (lock == null) {
            lock = this;
        }
        this.lock = lock;
    }

    public void await() {
        await(0);
    }

    public void await(long timeout) {
        synchronized (lock) {
            if (!isDone) {
                Util.wait(lock, timeout);
                this.timeout = !isDone;
            }
        }
    }

    @Override
    public void call(T res, Throwable ex) {
        synchronized (lock) {
            this.isDone = true;
            this.response = res;
            this.throwable = ex;
            lock.notify();
            if (timeout && res instanceof Closeable) {
                Util.close((Closeable) res);
            }
        }
    }

    public T getResponse() {
        return response;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public boolean isDone() {
        return isDone;
    }

    //not include timeout
    public boolean isFailed() {
        return throwable != null;
    }

    public boolean isTimeout() {
        return timeout;
    }

}
