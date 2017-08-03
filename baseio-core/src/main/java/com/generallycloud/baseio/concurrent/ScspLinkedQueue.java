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

public class ScspLinkedQueue<T extends Linkable<T>> implements LinkedQueue<T> {

	protected AtomicInteger	size	= new AtomicInteger();
	protected Linkable<T>	head	= null;				// volatile ?
	protected Linkable<T>	tail	= null;				// volatile ?

	public ScspLinkedQueue(Linkable<T> linkable) {
		linkable.setValidate(false);
		this.head = linkable;
		this.tail = linkable;
	}

	public void offer(Linkable<T> linkable) {
		tail.setNext(linkable);
		tail = linkable;
		size.incrementAndGet();
	}

	public T poll() {
		int size = size();
		if (size == 0) {
			return null;
		}
		return get(head);
	}
	
	private T get(Linkable<T> h){
		if (h.isValidate()) {
			Linkable<T> next = h.getNext();
			if (next == null) {
				h.setValidate(false);
			} else {
				head = next;
			}
			this.size.decrementAndGet();
			return h.getValue();
		} else {
			Linkable<T> next = h.getNext();
			head = next;
			return get(next);
		}
	}

	public int size() {
		return size.get();
	}

}
