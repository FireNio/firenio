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
    // head与tail的padding，64位模式运行不需要这么多 ，不确定伪共享是不是这么用的
    protected Linkable      headPadding0 = null;       
    protected Linkable      headPadding1 = null;       
    protected Linkable      headPadding2 = null;       
    protected Linkable      headPadding3 = null;
    protected Linkable      headPadding4 = null;
    protected Linkable      headPadding5 = null;
    protected Linkable      headPadding6 = null;
    protected Linkable      headPadding7 = null;
    protected Linkable      headPadding8 = null;
    protected Linkable      headPadding9 = null;       
    protected Linkable      headPadding10 = null;
    protected Linkable      headPadding11 = null;       
    protected Linkable      headPadding12 = null;       
    protected Linkable      headPadding13 = null;
    protected Linkable      headPadding14 = null;
    protected Linkable      headPadding15 = null;
    protected Linkable      tail = null;               // volatile ?

    public ScspLinkedQueue(Linkable linkable) {
        linkable.setValidate(false);
        this.head = linkable;
        this.tail = linkable;
    }

    @Override
    public void offer(Linkable linkable) {
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
            } else {
                head = next;
            }
            this.size.decrementAndGet();
            return (T) h;
        } else {
            Linkable next = h.getNext();
            head = next;
            return get(next);
        }
    }

    @Override
    public int size() {
        return size.get();
    }

}
