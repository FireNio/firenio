package com.gifisan.nio.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.gifisan.nio.common.DebugUtil;

public abstract class AbstractLinkedList<T> implements LinkedList<T> {

	protected int				_capability	= 0;
	protected Object[]			_array		= null;
	protected AtomicInteger		_size		= new AtomicInteger(0);
	protected AtomicInteger		_real_size	= new AtomicInteger(0);
	protected ReentrantLock		_lock		= new ReentrantLock();
	private Condition			_notEmpty		= _lock.newCondition();
	private AtomicBoolean 		_locked 		= new AtomicBoolean();
	
	public AbstractLinkedList(int _capability) {
		this._capability = _capability;
		this._array = new Object[_capability];
	}

	public AbstractLinkedList() {
		this(1024 * 8);
	}

	public boolean offer(T object) {
		int __size = _size.incrementAndGet();
		if (__size > _capability) {
			_size.decrementAndGet();
			return false;
		}

		int _c = incrementAndGet_end();

		_array[_c] = object;
		
		_real_size.incrementAndGet();

		if (_locked.get()) {
			
			final ReentrantLock _lock = this._lock;
			
			_lock.lock();
			
			if (_locked.compareAndSet(true, false)) {
				
				_notEmpty.signal();
				
			}
			
			_lock.unlock();
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	public T poll() {

		if (_real_size.get() == 0) {
			return null;
		}

		Object obj = _array[getAndIncrement_start()];
		
		_real_size.decrementAndGet();
		_size.decrementAndGet();

		return (T) obj;
	}

	@SuppressWarnings("unchecked")
	public T poll(long timeout) {

		if (_real_size.get() == 0) {
			
			final ReentrantLock _lock = this._lock;
			
			_lock.lock();
			
			try {
				
				_locked.set(true);
				
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

		_real_size.decrementAndGet();
		_size.decrementAndGet();

		return (T) obj;
	}

	public int size() {
		return _real_size.get();
	}

}
