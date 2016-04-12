package com.gifisan.nio.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.gifisan.nio.common.DebugUtil;

public abstract class AbstractLinkedList<T> implements LinkedList<T> {

	protected int				_capability	= 0;
	protected Object[]			_array		= null;
	protected byte[]			_empty_lock	= {};
	protected AtomicInteger		_size		= new AtomicInteger(0);
	protected ReentrantLock		_lock		= new ReentrantLock();
	private final Condition		_notEmpty		= _lock.newCondition();

	public AbstractLinkedList(int _capability) {
		this._capability = _capability;
		this._array = new Object[_capability];
	}

	public AbstractLinkedList() {
		this(1024 * 128);
	}

	public boolean offer(T object) {
		int __size = _size.incrementAndGet();
		if (__size > _capability) {
			_size.decrementAndGet();
			return false;
		}

		int _c = incrementAndGet_end();

		_array[_c] = object;

		if (__size == 1) {
			
			final ReentrantLock _lock = this._lock;
			
			_lock.lock();
			
			_notEmpty.signal();
			
			_lock.unlock();
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	public T poll() {

		if (_size.get() == 0) {
			return null;
		}

		Object obj = _array[getAndIncrement_start()];

		// _array[getAndIncrement_start()] = null;

		_size.decrementAndGet();

		return (T) obj;
	}

	@SuppressWarnings("unchecked")
	public T poll(long timeout) {

		if (_size.get() == 0) {
			
			final ReentrantLock _lock = this._lock;
			
			_lock.lock();
			
			try {
				_notEmpty.await(timeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e1) {
				_notEmpty.signal();
				DebugUtil.debug(e1);
			}
			
			_lock.unlock();
			
			return poll();
		}

		Object obj = _array[getAndIncrement_start()];

		// _array[getAndIncrement_start()] = null;

		_size.decrementAndGet();

		return (T) obj;
	}

	public int size() {
		return _size.get();
	}

}
