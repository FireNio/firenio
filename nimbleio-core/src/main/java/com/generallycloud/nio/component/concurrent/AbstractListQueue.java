package com.generallycloud.nio.component.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.common.MessageFormatter;

public abstract class AbstractListQueue<T> implements ListQueue<T> {

	protected int			_capability	;
	private Object[]		_array		;
	private AtomicInteger	_size		= new AtomicInteger(0);
	private ReentrantLock	_lock		= new ReentrantLock();
	private AtomicInteger	_real_size	= new AtomicInteger();
	private Condition		_notEmpty		= _lock.newCondition();
	private boolean		_locked		= false;
	private int			_start		;

	protected AbstractListQueue(int capability) {
		this._capability = capability;
		this._array = new Object[capability];
	}

	protected AbstractListQueue() {
		this(1024 * 8);
	}

	public boolean offer(T object) {
		if (!tryIncrementSize()) {
			return false;
		}

		int _c = getAndIncrementEnd();

		_array[_c] = object;

		_real_size.incrementAndGet();

		if (_locked) {

			final ReentrantLock _lock = this._lock;

			_lock.lock();

			_notEmpty.signal();

			_locked = false;

			_lock.unlock();
		}

		return true;
	}

	private boolean tryIncrementSize() {

		for (;;) {
			int __size = _size.get();
			
			int _next = __size + 1;

			if (_next > _capability) {
				return false;
			}

			if (_size.compareAndSet(__size, _next))
				return true;
		}
	}
	
	@SuppressWarnings("unchecked")
	public T poll() {

		if (_real_size.get() == 0) {
			return null;
		}

		int index = getAndincrementStart();

		Object obj = _array[index];

		_size.decrementAndGet();

		_real_size.decrementAndGet();

		return (T) obj;
	}

	@SuppressWarnings("unchecked")
	public T poll(long timeout) {

		if (_real_size.get() == 0) {

			final ReentrantLock _lock = this._lock;

			_lock.lock();

			try {
				_locked = true;

				_notEmpty.await(timeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e1) {

				e1.printStackTrace();

				_notEmpty.signal();

			}
			
			_locked = false;

			_lock.unlock();

			return poll();
		}

		int index = getAndincrementStart();

		Object obj = _array[index];

		_size.decrementAndGet();

		_real_size.decrementAndGet();

		return (T) obj;
	}

	public int size() {
		return _size.get();
	}
	
	protected int getAndincrementStart() {
		if (_start == _capability) {
			_start = 0;
		}
		return _start++;
	}
	
	protected abstract int getAndIncrementEnd();
	
	public String toString() {
		return MessageFormatter.format("capability {} , size {}", _capability,_real_size.get());
	}

}
