package com.gifisan.nio.component.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.gifisan.nio.common.MessageFormatter;

public abstract class AbstractLinkedList<T> implements LinkedList<T> {

	protected int			_capability	;
	private Object[]		_array		;
	private AtomicInteger	_size		= new AtomicInteger(0);
	private ReentrantLock	_lock		= new ReentrantLock();
	private AtomicInteger	_real_size	= new AtomicInteger();
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
		if (!tryIncrementSize()) {
			return false;
		}

		int _c = getAndincrementEnd();

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

	private void forceIncrementSize() {

		for (;;) {
			int __size = _size.get();
			
			int _next = __size + 1;

			if (_next > _capability) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}

			if (_size.compareAndSet(__size, _next))
				return;
		}
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
	
	

	public void forceOffer(T object) {

		forceIncrementSize();

		int _c = getAndincrementEnd();

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
	
	protected abstract int getAndincrementStart();

	protected abstract int getAndincrementEnd();
	
	public String toString() {
		return MessageFormatter.format("capability {} , size {}", _capability,_real_size.get());
	}

}
