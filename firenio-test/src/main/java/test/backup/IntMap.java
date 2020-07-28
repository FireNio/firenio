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
package test.backup;

import com.firenio.common.Util;

import java.util.Arrays;

/**
 * @author wangkai
 */
public final class IntMap<V> {

    static final float DEFAULT_LOAD_FACTOR = 0.5f;

    private final float loadFactor;
    private       int   cap;
    private       int[] keys;
    private       V[]   values;
    private       int   scan_size;
    private       int   size;
    private       int   mask;
    private       int   scan_index;
    private       int   limit;

    public IntMap() {
        this(16);
    }

    public IntMap(int cap) {
        this(cap, DEFAULT_LOAD_FACTOR);
    }

    @SuppressWarnings("unchecked")
    public IntMap(int cap, float loadFactor) {
        cap = Math.max(16, cap);
        int c = Util.clothCover(cap);
        this.cap = c;
        this.mask = c - 1;
        this.loadFactor = loadFactor;
        this.keys = new int[c];
        this.values = (V[]) new Object[c];
        this.limit = (int) (c * loadFactor);
        Arrays.fill(keys, -1);
    }

    /**
     * DO NOT REMOVE while scan
     */
    public void scan() {
        this.scan_size = 0;
        this.scan_index = -1;
    }

    public V putIfAbsent(int key, V value) {
        V v = get(key);
        if (v == null) {
            v = put(key, value);
        }
        return v;
    }

    public V put(int key, V value) {
        V[]   values      = this.values;
        int[] keys        = this.keys;
        int   mask        = this.mask;
        int   start_index = key & mask;
        int   index       = start_index;
        for (; ; ) {
            int _key = keys[index];
            if (_key == -1) {
                keys[index] = key;
                values[index] = value;
                grow();
                return null;
            } else if (_key == key) {
                V old = values[index];
                values[index] = value;
                return old;
            }
            if ((index = next_key(index, mask)) == start_index) {
                throw new IllegalStateException("failed to put");
            }
        }
    }

    private static int next_key(int key, int mask) {
        return (key + 1) & mask;
    }

    private void put(int[] keys, V[] values, int key, V value, int mask) {
        int start_index = key & mask;
        int index       = start_index;
        for (; ; ) {
            int _key = keys[index];
            if (_key == -1) {
                keys[index] = key;
                values[index] = value;
                break;
            } else if (_key == key) {
                values[index] = value;
                break;
            }
            if ((index = next_key(index, mask)) == start_index) {
                throw new IllegalStateException("failed to put");
            }
        }
    }

    public boolean hasNext() {
        return next() != -1;
    }

    private int next() {
        if (scan_size < size) {
            int[] keys  = this.keys;
            int   index = this.scan_index + 1;
            int   cap   = this.cap;
            for (; index < cap; index++) {
                if (keys[index] != -1) {
                    break;
                }
            }
            this.scan_size++;
            this.scan_index = index;
            return index;
        }
        return -1;
    }

    public int key() {
        return keys[scan_index];
    }

    public V value() {
        return values[scan_index];
    }

    public int indexKey(int index) {
        return keys[index];
    }

    public V indexValue(int index) {
        return values[index];
    }

    @SuppressWarnings("unchecked")
    private void grow() {
        size++;
        if (size > limit) {
            int   cap    = Util.clothCover(this.cap + 1);
            int   mask   = cap - 1;
            int[] keys   = new int[cap];
            V[]   values = (V[]) new Object[cap];
            int   limit  = (int) (cap * loadFactor);
            Arrays.fill(keys, -1);
            scan();
            int size = 0;
            for (; ; ) {
                int index = next();
                if (index == -1) {
                    break;
                }
                size++;
                put(keys, values, indexKey(index), indexValue(index), mask);
            }
            assert size == this.size;
            this.cap = cap;
            this.mask = mask;
            this.keys = keys;
            this.values = values;
            this.limit = limit;
        }
    }

    public V get(int key) {
        int[] keys        = this.keys;
        int   mask        = this.mask;
        int   start_index = key & mask;
        int   index       = start_index;
        for (; ; ) {
            int _key = keys[index];
            if (_key == -1) {
                return null;
            } else if (_key == key) {
                return values[index];
            }
            if ((index = next_key(index, mask)) == start_index) {
                return null;
            }
        }
    }

    // ref from IdentityHashMap
    private void remove_at(int[] keys, V[] values, final int index, int mask) {
        // Adapted from Knuth Section 6.4 Algorithm R
        // Look for items to swap into newly vacated slot
        // starting at index immediately following deletion,
        // and continuing until a null slot is seen, indicating
        // the end of a run of possibly-colliding keys.
        for (int i = next_key(index, mask), key, next = index; (key = keys[i]) != -1; i = next_key(i, mask)) {
            // The following test triggers if the item at slot i (which
            // hashes to be at slot r) should take the spot vacated by d.
            // If so, we swap it in, and then continue with d now at the
            // newly vacated i.  This process will terminate when we hit
            // the null slot at the end of this run.
            // The test is messy because we are using a circular table.
            int r = key & mask;
            if ((i < r && (r <= next || next <= i)) || (r <= next && next <= i)) {
                keys[next] = key;
                values[next] = values[i];
                keys[i] = -1;
                values[i] = null;
                next = i;
            }
        }
    }

    public V remove(int key) {
        int[] keys        = this.keys;
        int   mask        = this.mask;
        int   start_index = key & mask;
        int   index       = start_index;
        for (; ; ) {
            int _key = keys[index];
            if (_key == -1) {
                return null;
            } else if (_key == key) {
                size--;
                V value = values[index];
                keys[index] = -1;
                values[index] = null;
                remove_at(keys, values, index, mask);
                return value;
            }
            if ((index = next_key(index, mask)) == start_index) {
                return null;
            }
        }
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
                sb.append(keys[i]).append('=').append(value == this ? "(this Map)" : value);
            }
        }
        return sb.append('}').toString();
    }

    public int conflict() {
        int s = 0;
        scan();
        for (; ; ) {
            int index = next();
            if (index == -1) {
                break;
            }
            if ((key() & mask) != index) {
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
