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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author wangkai
 *
 */
public class AtomicLockImpl implements Lock {

    private AtomicBoolean lock = new AtomicBoolean();

    @Override
    public boolean tryLock() {
        return lock.compareAndSet(false, true);
    }

    @Override
    public void unlock() {
        lock.set(false);
    }

    @Override
    public void lock() {
        AtomicBoolean lock = this.lock;
        if (!lock.compareAndSet(false, true)) {
            for (; !lock.compareAndSet(false, true);) {}
        }
    }

}
