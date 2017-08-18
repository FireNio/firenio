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
package com.generallycloud.baseio.common;

/**
 * @author wangkai
 *
 */
public class BitSet {

    private byte[]        data;

    private int           capacity;

    private transient int wordsInUse = 0;

    public BitSet(int capacity) {
        if ((capacity & 7) != 0) {
            throw new IllegalArgumentException("(capacity & 7) != 0");
        }
        this.capacity = capacity;
        this.data = new byte[capacity >> 3];
    }

    public void set(int index) {
        int idx = index >> 3;
        data[idx] |= (0b10000000 >> (index & 7));
        expandTo(idx);
    }

    public void clear(int index) {
        data[index >> 3] &= ~(0b10000000 >> (index & 7));
        recalculateWordsInUse();
    }

    public boolean get(int index) {
        int idx = index >> 3;
        return (idx < wordsInUse) && (data[idx] & (0b10000000 >> (index & 7))) != 0;
    }

    public int getCapacity() {
        return capacity;
    }

    private void recalculateWordsInUse() {
        int i;
        for (i = wordsInUse - 1; i >= 0; i--) {
            if (data[i] != 0) {
                break;
            }
        }
        wordsInUse = i + 1;
    }

    private void expandTo(int idx) {
        int wordsRequired = idx + 1;
        if (wordsInUse < wordsRequired) {
            wordsInUse = wordsRequired;
        }
    }

}
