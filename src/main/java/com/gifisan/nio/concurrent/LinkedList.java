package com.gifisan.nio.concurrent;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.atomic.AtomicInteger;

import sun.misc.Unsafe;

public class LinkedList<T> {

	private int				_capability	= 0;
	private AtomicInteger		_end			= new AtomicInteger(-1);
	private int				_start		= 0;
	private byte[]			_empty_lock	= {};
	private AtomicInteger		_size		= new AtomicInteger(0);
	private RingBufferFields		_bufferFields	= null;

	public LinkedList(int capability) {
		this._capability = capability;
		this._bufferFields = new RingBufferFields(capability);
	}

	public LinkedList() {
		this(1024 << 3);
	}

	public boolean offer(T object) {
		int __size = _size.incrementAndGet();
		if (__size > _capability) {
			_size.decrementAndGet();
			return false;
		}

		int _c = incrementAndGet_end();

		PlaceHolder placeHolder = _bufferFields.elementAt(_c);

		placeHolder.t = object;

		if (__size == 1) {
			synchronized (_empty_lock) {
				_empty_lock.notify();
			}
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	public T poll() {

		if (_size.get() == 0) {
			return null;
		}

		PlaceHolder placeHolder = _bufferFields.elementAt(_start);

		Object obj = placeHolder.t;

		getAndIncrement_start();

		_size.decrementAndGet();

		return (T) obj;
	}

	@SuppressWarnings("unchecked")
	public T poll(long timeout) {

		if (_size.get() == 0) {
			synchronized (_empty_lock) {
				try {
					_empty_lock.wait(timeout);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return poll();
			}
		}

		PlaceHolder placeHolder = _bufferFields.elementAt(_start);

		Object obj = placeHolder.t;

		getAndIncrement_start();

		_size.decrementAndGet();

		return (T) obj;
	}

	private int getAndIncrement_start() {
		if (_start == _capability) {
			_start = 0;
		}
		return _start++;
	}

	private final int incrementAndGet_end() {
		for (;;) {
			int current = _end.get();

			int next = current + 1;

			if (next == _capability) {
				next = 0;
			}

			if (_end.compareAndSet(current, next))
				return next;
		}
	}

	class PlaceHolder {
		T	t	= null;
	}

	class RingBufferFields {
		private int		BUFFER_PAD		= 0;
		private int		REF_ARRAY_BASE		= 0;
		private int		REF_ELEMENT_SHIFT	= 0;
		private long		indexMask			= 0;
		private int		bufferSize		= 0;
		private Object[]	entries			= null;
		private Unsafe		UNSAFE			= GetUnSafe.getUnsafe();

		RingBufferFields(int bufferSize) {

			int scale = UNSAFE.arrayIndexScale(Object[].class);
			if (4 == scale) {
				REF_ELEMENT_SHIFT = 2;
			} else if (8 == scale) {
				REF_ELEMENT_SHIFT = 3;
			} else {
				throw new IllegalStateException("Unknown pointer size");
			}
			BUFFER_PAD = 128 / scale;
			// Including the buffer pad in the array base offset
			REF_ARRAY_BASE = UNSAFE.arrayBaseOffset(Object[].class) + (BUFFER_PAD << REF_ELEMENT_SHIFT);

			this.bufferSize = bufferSize;
			if (Integer.bitCount(bufferSize) != 1) {
				throw new IllegalArgumentException("bufferSize must be a power of 2");
			}
			this.indexMask = bufferSize - 1;
			this.entries = new Object[bufferSize + 2 * BUFFER_PAD];
			fill();
		}

		private void fill() {
			for (int i = 0; i < bufferSize; i++) {
				entries[BUFFER_PAD + i] = new PlaceHolder();
			}
		}

		@SuppressWarnings("unchecked")
		protected final PlaceHolder elementAt(int index) {
			return (PlaceHolder) UNSAFE.getObject(entries, REF_ARRAY_BASE
					+ ((index & indexMask) << REF_ELEMENT_SHIFT));
		}


	}
	
	static class GetUnSafe{
		
		private static final Unsafe	THE_UNSAFE;

		static {
			try {
				final PrivilegedExceptionAction<Unsafe> action = new PrivilegedExceptionAction<Unsafe>() {
					public Unsafe run() throws Exception {
						Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
						theUnsafe.setAccessible(true);
						return (Unsafe) theUnsafe.get(null);
					}
				};

				THE_UNSAFE = AccessController.doPrivileged(action);
			} catch (Exception e) {
				throw new RuntimeException("Unable to load unsafe", e);
			}
		}

		static Unsafe getUnsafe() {
			return THE_UNSAFE;
		}
	}
}
