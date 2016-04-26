package com.gifisan.nio.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractLinkedList<T> implements LinkedList<T> {

	protected int			_capability	= 0;
	protected Object[]		_array		= null;
	protected AtomicInteger	_size		= new AtomicInteger(0);
	protected ReentrantLock	_lock		= new ReentrantLock();
	private AtomicInteger			_real_size	= new AtomicInteger();
	private Condition		_notEmpty		= _lock.newCondition();
	private boolean		_locked		= false;

	public AbstractLinkedList(int _capability) {
		this._capability = _capability;
		this._array = new Object[_capability];
	}

	public AbstractLinkedList() {
		this(1024 * 8);
	}

	public boolean offer(T object) {
		int __size = _size.get();

		if (__size == _capability) {
			return false;
		}

		if(!_size.compareAndSet(__size, ++__size)){
			return false;
		}
		
		int _c = incrementAndGet_end();

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

	private void incrementSize() {

		for (;;) {
			int __size = _size.get();

			if (__size > _capability) {
				continue;
			}

			if (_size.compareAndSet(__size, ++__size))
				return;
		}
	}

	public void forceOffer(T object) {

		incrementSize();

		int _c = incrementAndGet_end();

		_array[_c] = object;

		_real_size.incrementAndGet();
		
		if (_locked) {

			final ReentrantLock _lock = this._lock;

			_lock.lock();

			_notEmpty.signal();

			_locked = false;

			_lock.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	public T poll() {

		if (_real_size.get() == 0) {
			return null;
		}

		int index = getAndIncrement_start();

		Object obj = _array[index];

		for (; obj == null;) {

			obj = _array[index];
		}

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

			_lock.unlock();

			return poll();
		}

		int index = getAndIncrement_start();

		Object obj = _array[index];

		for (; obj == null;) {
			obj = _array[index];
		}

		_size.decrementAndGet();
		
		_real_size.decrementAndGet();

		return (T) obj;
	}

	public int size() {
		return _size.get();
	}

}
