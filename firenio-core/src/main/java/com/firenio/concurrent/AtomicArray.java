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

import com.firenio.common.Unsafe;

/**
 * copy from java.util.concurrent.atomic.AtomicReferenceArray
 */
public final class AtomicArray {

    private static final int      base;
    private static final int      shift;
    private static final long     arrayFieldOffset;
    private final        Object[] array;

    static {
        try {
            arrayFieldOffset = Unsafe.fieldOffset(AtomicArray.class.getDeclaredField("array"));
            base = Unsafe.arrayBaseOffset(Object[].class);
            int scale = Unsafe.arrayIndexScale(Object[].class);
            if ((scale & (scale - 1)) != 0)
                throw new Error("data type scale not a power of two");
            shift = 31 - Integer.numberOfLeadingZeros(scale);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private static long offset(int i) {
        return ((long) i << shift) + base;
    }

    public AtomicArray(int length) {
        array = new Object[length];
    }

    public int length() {
        return array.length;
    }

    public Object get(int index) {
        return array[index];
    }

    public Object getVolatile(int index) {
        return Unsafe.getObjectVolatile(array, offset(index));
    }

    public void set(int i, Object value) {
        array[i] = value;
    }

    public void setVolatile(int i, Object value) {
        Unsafe.putObjectVolatile(array, offset(i), value);
    }

    public void lazySet(int i, Object value) {
        Unsafe.putOrderedObject(array, offset(i), value);
    }

    public Object getAndSet(int i, Object value) {
        return Unsafe.getAndSetObject(array, offset(i), value);
    }

    public boolean compareAndSet(int i, Object expect, Object update) {
        return Unsafe.compareAndSwapObject(array, offset(i), expect, update);
    }

}
