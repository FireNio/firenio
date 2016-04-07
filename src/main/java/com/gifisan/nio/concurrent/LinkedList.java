package com.gifisan.nio.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

public class LinkedList<T> {

	private Array		_current_array	= null;
	private Array		_other_array	= null;
	private int		_capability	= 1024 * 5000;

	public LinkedList() {
		this(1024 * 5000);
	}

	public LinkedList(int capability) {
//		if (capability < _capability) {
//			throw new IllegalArgumentException("the min size,"+this._capability);
//		}
		this._capability = capability;
		this._current_array = new Array();
		this._other_array = new Array();
	}

	public void offer(T object) {
		_current_array.offer(object, _other_array);
	}

	public T poll() {
		return _current_array.poll();
	}

	class Array {

		private AtomicInteger	_end		= new AtomicInteger(-1);
		private int			_start	= 0;
		private Object[]		_array	= new Object[_capability];
		private byte [] 		_lock	= {0};
		private byte []		__lock 	= null;

		public void offer(T object, Array other) {
			int _c = _end.incrementAndGet();
			if (_c < _capability) {
				_array[_c] = object;
			} else if (_c > _capability) {
				other.offer(object, this);
			} else if (_c == _capability) {
				other.offer(object, this);
			}
			byte [] __lock = this.__lock;
			if (__lock != null) {
				synchronized (__lock) {
					__lock.notify();
				}
			}
		}

		public T poll() {
			if (this._start == _capability) {
				this._start = 0;
				Array _temp = _current_array;
				_current_array = _other_array;
				_other_array = _temp;
				_current_array._end.set(-1);
				return null;
			}
			Object obj = _array[_start];
			if (obj == null) {
				synchronized (_lock) {
					this.__lock = _lock;
					try {
						__lock.wait(16);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				return poll0();
			}
			_array[_start++] = null;
			return (T) obj;
		}
		
		public T poll0(){
			Object obj = _array[_start];
			if (obj == null) {
				return null;
			}
			_array[_start++] = null;
			return (T) obj;
		}
	}
}
