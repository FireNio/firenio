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
package com.firenio.collection;

import com.firenio.collection.AttributeMap.AttributeInitFunction;

/**
 * @author: wangkai
 **/
public final class AttributeKey<T> {

    private final int index;

    private final String name;

    private final AttributeInitFunction function;

    AttributeKey(int index, String name, AttributeInitFunction function) {
        this.name = name;
        this.index = index;
        this.function = function;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public AttributeInitFunction getFunction() {
        return function;
    }

    @Override
    public String toString() {
        return "name: " + name + ", index: " + index;
    }

}
