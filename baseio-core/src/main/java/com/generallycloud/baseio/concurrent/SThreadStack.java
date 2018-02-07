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

import java.util.Arrays;

/**
 * @author wangkai
 *
 */
public class SThreadStack<T> {

    private T[]              elements;

    private int              size;

    private ObjectFactory<T> factory;

    @SuppressWarnings("unchecked")
    public SThreadStack(int size, ObjectFactory<T> factory) {
        this.elements = (T[]) new Object[size];
        this.factory = factory;
    }

    public SThreadStack(ObjectFactory<T> factory) {
        this(16, factory);
    }

    public T pop() {
        if (size == 0) {
            return factory.create();
        }
        return elements[--size];
    }

    public void push(T t) {
        if (size == elements.length) {
            grow(size);
        }
        elements[size++] = t;
    }

    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elements.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity < 0)
            throw new ArrayIndexOutOfBoundsException("grow failed");
        elements = Arrays.copyOf(elements, newCapacity);
    }

    public int size() {
        return size;
    }

}
