/*
 * Copyright 2015 The Baseio Project
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
package com.firenio.baseio.collection;

import java.util.Arrays;

import com.firenio.baseio.common.Util;

/**
 * @author wangkai
 *
 */
public final class IntMap<V> {

    private int         cap;
    private int[]       keys;
    private V[]         values;
    private int         scanSize;
    private int         size;
    private int         mask;
    private int         scanIndex;
    private int         limit;
    private final float loadFactor;

    public IntMap() {
        this(16);
    }

    public IntMap(int cap) {
        this(cap, 0.75f);
    }

    @SuppressWarnings("unchecked")
    public IntMap(int cap, float loadFactor) {
        cap = Math.max(16, cap);
        int c = Util.clothCover(cap);
        this.cap = c;
        this.mask = c - 1;
        this.loadFactor = Math.min(loadFactor, 0.75f);
        this.keys = new int[c];
        this.values = (V[]) new Object[c];
        this.limit = (int) (c * loadFactor);
        Arrays.fill(keys, -1);
    }

    public void scan() {
        this.scanSize = 0;
        this.scanIndex = -1;
    }

    public boolean hasNext() {
        return scanSize < size;
    }

    private static int indexOfKey(int[] keys, int key, int mask) {
        int index = key & mask;
        if (keys[index] == key) {
            return index;
        }
        for (int i = index, cnt = keys.length; i < cnt; i++) {
            if (keys[i] == key) {
                return i;
            }
        }
        for (int i = 1; i < index; i++) {
            if (keys[i] == key) {
                return i;
            }
        }
        //will not happen
        return -1;

    }

    private static int indexOfFreeKey(int[] keys, int key, int mask) {
        int index = key & mask;
        int _key = keys[index];
        if (_key == -1 || _key == key) {
            return index;
        }
        for (int i = index, cnt = keys.length; i < cnt; i++) {
            _key = keys[i];
            if (_key == -1 || _key == key) {
                return i;
            }
        }
        for (int i = 1; i < index; i++) {
            _key = keys[i];
            if (_key == -1 || _key == key) {
                return i;
            }
        }
        //will not happen
        return -1;

    }

    public V putIfAbsent(int key, V value) {
        return putVal(key, value, true);
    }

    private V putVal(int key, V value, boolean absent) {
        V res = put0(key, value, mask, keys, values, absent);
        if (res == null) {
            grow();
            return null;
        }
        return res;
    }

    public V put(int key, V value) {
        return putVal(key, value, false);
    }

    private int scan(int[] keys, int index) {
        for (int i = index + 1, cnt = keys.length; i < cnt; i++) {
            if (keys[i] != -1) {
                return i;
            }
        }
        return keys.length;
    }

    public int next() {
        int scanIndex = scan(keys, this.scanIndex);
        if (scanIndex == cap) {
            return scanIndex;
        }
        this.scanIndex = scanIndex;
        this.scanSize++;
        return scanIndex;
    }

    public int nextKey() {
        next();
        return key();
    }

    public V nextValue() {
        next();
        return value();
    }

    public int key() {
        return keys[scanIndex];
    }

    public V value() {
        return values[scanIndex];
    }

    public int indexKey(int index) {
        return keys[index];
    }

    public V indexValue(int index) {
        return values[index];
    }

    private V put0(int key, V value, int mask, int[] keys, V[] values, boolean absent) {
        int index = indexOfFreeKey(keys, key, mask);
        if (keys[index] == key) {
            if (absent) {
                return values[index];
            } else {
                V old = values[index];
                values[index] = value;
                return old;
            }
        } else {
            keys[index] = key;
            values[index] = value;
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private void grow() {
        size++;
        if (size > limit) {
            int c = Util.clothCover(cap + 1);
            int cap = c;
            int mask = c - 1;
            int[] keys = new int[c];
            V[] values = (V[]) new Object[c];
            int limit = (int) (c * loadFactor);
            Arrays.fill(keys, -1);
            scan();
            for (; hasNext();) {
                int index = next();
                put0(indexKey(index), indexValue(index), mask, keys, values, false);
            }
            this.cap = cap;
            this.mask = mask;
            this.keys = keys;
            this.values = values;
            this.limit = limit;
        }
    }

    public V get(int key) {
        int index = indexOfKey(keys, key, mask);
        if (index == -1) {
            return null;
        }
        return values[index];
    }

    public V remove(int key) {
        int index = indexOfKey(keys, key, mask);
        if (index == -1) {
            return null;
        }
        V v = values[index];
        values[index] = null;
        keys[index] = -1;
        size--;
        if (index <= scanIndex) {
            scanSize--;
        }
        return v;
    }

    public String toString() {
        if (size == 0) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder(4 * size);
        for (int i = 0; i < values.length; ++i) {
            V value = values[i];
            if (value != null) {
                sb.append(sb.length() == 0 ? "{" : ", ");
                sb.append(Integer.toString(keys[i])).append('=')
                        .append(value == this ? "(this Map)" : value);
            }
        }
        return sb.append('}').toString();
    }

    public int conflict() {
        int s = 0;
        scan();
        for (; hasNext();) {
            int index = next();
            if ((keys[index] & mask) != index) {
                s++;
            }
        }
        return s;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void clear() {
        Arrays.fill(keys, -1);
        Arrays.fill(values, null);
        size = 0;
    }

}
