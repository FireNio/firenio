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

import com.generallycloud.baseio.component.Linkable;

public class ListQueueLinkUnsafe<T extends Linkable<T>> implements ListQueue<T> {

	private int		size;

	private Linkable<T>	head	= null;

	private Linkable<T>	tail	= null;

	@Override
	public boolean offer(T object) {

		if (size == 0) {
			head = tail = object;
		} else {
			tail.setNext(object);
			tail = object;
		}
		size++;
		return true;
	}

	@Override
	public T poll() {

		if (size == 0) {
			return null;
		}
	
		return get();
	}

	@Override
	public T poll(long timeout) {
		throw new UnsupportedOperationException();
	}

	private T get() {

		Linkable<T> t = head;
		Linkable<T> next = t.getNext();

		if (next == null) {
			head = tail = null;
		} else {
			head = next;
		}

		size--;
		return t.getValue();

	}

	@Override
	public int size() {
		return size;
	}

}
