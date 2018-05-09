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

import java.util.concurrent.atomic.AtomicInteger;

public class ScspLinkedQueue<T> implements LinkedQueue<T> {

    protected AtomicInteger size = new AtomicInteger();
    protected Linkable      head = null;               // volatile ?
    protected Linkable      tail = null;               // volatile ?

    public ScspLinkedQueue(Linkable linkable) {
        linkable.setValidate(false);
        this.head = linkable;
        this.tail = linkable;
    }

    @Override
    public void offer(Linkable linkable) {
        linkable.setValidate(true);
        if (linkable == tail) {
            size.incrementAndGet();
            return;
        }
        tail.setNext(linkable);
        tail = linkable;
        size.incrementAndGet();
    }

    @Override
    public T poll() {
        int size = size();
        if (size == 0) {
            return null;
        }
        return get(head);
    }

    @SuppressWarnings("unchecked")
    private T get(Linkable h) {
        if (h.isValidate()) {
            Linkable next = h.getNext();
            if (next == null) {
                h.setValidate(false);
                head = h;
            } else {
                head = next;
            }
            this.size.decrementAndGet();
            return (T) h;
        } else {
            return get(h.getNext());
        }
    }

    @Override
    public int size() {
        return size.get();
    }
    
}
