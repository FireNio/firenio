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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.baseio.common.MessageFormatter;

public abstract class AbstractListQueue<T> implements ListQueue<T> {

	protected int			capability;
	private volatile T[]	array;
	private AtomicInteger	size	= new AtomicInteger(0);
	private ReentrantLock	lock	= new ReentrantLock();
	private Condition		notEmpty	= lock.newCondition();
	private volatile boolean	locked	= false;
	private int			start;

	@SuppressWarnings("unchecked")
	protected AbstractListQueue(int capability) {
		this.capability = capability;
		this.array = (T[]) new Object[capability];
	}

	protected AbstractListQueue() {
		this(1024 * 8);
	}

	@Override
	public boolean offer(T object) {
		
		if (!tryIncrementSize()) {
			return false;
		}

		array[getAndIncrementEnd()] = object;
		
		if (locked) {

			ReentrantLock _lock = this.lock;

			_lock.lock();

			try {
				notEmpty.signal();
			} catch (Exception e) {
			}

			locked = false;

			_lock.unlock();
		}

		return true;
	}

	private boolean tryIncrementSize() {
		
		int _size = size.incrementAndGet();
		
		if (_size > capability) {
			size.decrementAndGet();
			return false;
		}
		
		return true;
	}

	private boolean hasElement() {

		int _size = size.decrementAndGet();

		if (_size < 0) {
			size.incrementAndGet();
			return false;
		}

		return true;
	}

	@Override
	public T poll() {

		if (!hasElement()) {
			return null;
		}

		return getObject(getAndincrementStart());
	}

	@Override
	public T poll(long timeout) {

		if (size() == 0) {

			final ReentrantLock _lock = this.lock;

			_lock.lock();

			try {
				locked = true;

				notEmpty.await(timeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {

				notEmpty.signal();
			}

			locked = false;

			_lock.unlock();
		}

		return poll();
	}

	private T getObject(int index) {
		T obj = array[index];
		if (obj == null) {
			for (;;) {
				obj = array[index];
				if (obj == null) {
					continue;
				}
				return obj;
			}
		}
		return obj;
	}

	@Override
	public int size() {
		return size.get();
	}

	protected int getAndincrementStart() {
		if (start == capability) {
			start = 0;
		}
		return start++;
	}

	protected abstract int getAndIncrementEnd();

	@Override
	public String toString() {
		return MessageFormatter.format("capability {} , size {}", capability, size.get());
	}

}
