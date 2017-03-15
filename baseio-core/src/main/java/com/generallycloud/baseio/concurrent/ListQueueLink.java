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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.baseio.component.Linkable;

public class ListQueueLink<T extends Linkable<T>> implements ListQueue<T> {

	private ReentrantLock	lock	= new ReentrantLock();
	
	private Condition empty = lock.newCondition();

	private int			size;

	private Linkable<T>			head	= null;

	private Linkable<T>			tail	= null;

	@Override
	public boolean offer(T object) {

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			if (size == 0) {
				head = tail = object;
				empty.signal();
			} else {
				tail.setNext(object);
				tail = object;
			}
			size++;
			return true;
		} finally {

			lock.unlock();
		}
	}

	@Override
	public T poll() {

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			if (size == 0) {
				return null;
			}

			return get();
			
		} finally {
			lock.unlock();
		}
	}

	@Override
	public T poll(long timeout) {
		
		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			if (size == 0) {
				
				try {
					empty.await(timeout, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					empty.signal();
				}
				
				if (size == 0) {
					return null;
				}
				
				return get();
			}

			return get();

		} finally {
			lock.unlock();
		}
	}
	
	private T get(){
		
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
