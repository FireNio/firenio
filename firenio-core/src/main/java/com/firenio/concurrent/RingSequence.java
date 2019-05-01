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

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * @program: firenio
 * @description:
 * @author: wangkai
 * @create: 2019-05-30 13:17
 **/
public class RingSequence {

    private static final AtomicIntegerFieldUpdater SEQ_UPDATER;
    private final        int                       max;
    private final        int                       min;
    private volatile     int                       seq;

    static {
        SEQ_UPDATER = AtomicIntegerFieldUpdater.newUpdater(RingSequence.class, "seq");
    }

    public RingSequence(int max) {
        this(0, max);
    }

    public RingSequence(int min, int max) {
        this.seq = min;
        this.min = min;
        this.max = max;
    }

    public int next() {
        for (; ; ) {
            int seq  = this.seq;
            int next = seq + 1;
            if (next == max) {
                next = min;
            }
            if (SEQ_UPDATER.compareAndSet(this, seq, next)) {
                return seq;
            }
        }
    }


}
