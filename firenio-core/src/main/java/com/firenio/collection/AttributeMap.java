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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.firenio.concurrent.AtomicArray;

/**
 * use for static variable, eg:
 * <pre>
 * static final AttributeKey KEY_NAME = AttributeMap.valueOfKey(Channel.class, "KEY_NAME");
 * <pre/>
 */
//TODO 考虑多个ChannelContext时，每个Context里Channel存储的数据key不一样，会造成内存浪费
public abstract class AttributeMap {

    private static final Map<Class<?>, AttributeKeys> INDEX_MAPPING = new ConcurrentHashMap<>();

    private final AtomicArray attributes;

    public AttributeMap() {
        AttributeKeys keys = getKeys();
        if (keys == null) {
            attributes = null;
            return;
        }
        keys.initialized = true;
        this.attributes = new AtomicArray(keys.getCounter());
        for (AttributeKey key : keys.keys.values()) {
            AttributeInitFunction function = key.getFunction();
            function.setValue(this, key.getIndex());
        }
    }

    public <T> T getAttribute(AttributeKey<T> key) {
        return (T) getAttribute(key.getIndex());
    }

    public <T> T getAttributeUnsafe(AttributeKey<T> key) {
        return (T) getAttributeUnsafe(key.getIndex());
    }

    public Object getAttribute(int key) {
        return attributes.getVolatile(key);
    }

    public Object getAttributeUnsafe(int key) {
        return attributes.get(key);
    }

    public void setAttribute(AttributeKey key, Object value) {
        setAttribute(key.getIndex(), value);
    }

    public void setAttributeUnsafe(AttributeKey key, Object value) {
        setAttributeUnsafe(key.getIndex(), value);
    }

    public void setAttribute(int key, Object value) {
        attributes.setVolatile(key, value);
    }

    public void setAttributeUnsafe(int key, Object value) {
        attributes.set(key, value);
    }

    public static AttributeKeys getKeys(Class clazz) {
        return INDEX_MAPPING.get(clazz);
    }

    public static AttributeKey valueOfKey(Class clazz, String name) {
        return valueOfKey(clazz, name, null);
    }

    public static AttributeKey valueOfKey(Class clazz, String name, AttributeInitFunction function) {
        AttributeKeys attributeKeys = INDEX_MAPPING.get(clazz);
        if (attributeKeys == null) {
            synchronized (INDEX_MAPPING) {
                attributeKeys = INDEX_MAPPING.get(clazz);
                if (attributeKeys == null) {
                    attributeKeys = new AttributeKeys();
                    INDEX_MAPPING.put(clazz, attributeKeys);
                }
            }
        }
        return attributeKeys.valueOf(name, function);
    }

    protected abstract AttributeKeys getKeys();

    public static class AttributeKeys {

        final Map<String, AttributeKey> keys    = new HashMap<>();
        final Map<String, AttributeKey> ro_keys = Collections.unmodifiableMap(keys);

        int counter = 0;
        volatile boolean initialized = false;

        synchronized AttributeKey valueOf(String name, AttributeInitFunction function) {
            if (initialized) {
                throw new RuntimeException("incorrect usage, AttributeKey can only be static constant");
            }
            if (function == null) {
                function = NULL_VALUE_ATTRIBUTE_INIT_FUNCTION;
            }
            keys.put(name, new AttributeKey(counter++, name, function));
            return keys.get(name);
        }

        public Map<String, AttributeKey> getKeys() {
            return ro_keys;
        }

        public int getCounter() {
            return counter;
        }

    }

    public interface AttributeInitFunction {

        void setValue(AttributeMap map, int key);

    }

    static final AttributeInitFunction NULL_VALUE_ATTRIBUTE_INIT_FUNCTION = new AttributeInitFunction() {
        @Override
        public void setValue(AttributeMap map, int key) { }
    };

}
