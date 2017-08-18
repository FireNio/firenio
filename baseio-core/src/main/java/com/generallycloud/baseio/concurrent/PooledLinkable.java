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

import com.generallycloud.baseio.component.AbstractLinkable;

/**
 * @author wangkai
 *
 */
public class PooledLinkable<V> extends AbstractLinkable {

    private PooledLinkable<V> next;

    private V                 value;

    @Override
    public PooledLinkable<V> getNext() {
        return next;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setNext(Linkable next) {
        this.next = (PooledLinkable<V>) next;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.reset();
        this.value = value;
    }

    public void reset() {
        this.next = null;
        this.setValidate(true);
    }

}
