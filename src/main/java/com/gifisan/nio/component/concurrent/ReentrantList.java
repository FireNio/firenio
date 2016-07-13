package com.gifisan.nio.component.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantList<T> {

	private ArrayList<T>	snapshot	= new ArrayList<T>();

	private ReentrantLock	lock		= new ReentrantLock();

	public int size() {

		return snapshot.size();
	}

	public boolean isEmpty() {

		return snapshot.isEmpty();
	}

	public boolean contains(Object o) {

		return snapshot.contains(o);
	}

	public T get(int index) {

		return snapshot.get(index);
	}

	public boolean add(T e) {

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			return snapshot.add(e);
		} finally {
			lock.unlock();
		}
	}

	public T remove(int index) {
		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			return snapshot.remove(index);
		} finally {
			lock.unlock();
		}
	}

	public boolean remove(Object o) {

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			return snapshot.remove(o);
		} finally {
			lock.unlock();
		}
	}

	public void clear() {

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			snapshot.clear();
		} finally {
			lock.unlock();
		}
	}

	public boolean addAll(Collection<? extends T> c) {

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			return snapshot.addAll(c);
		} finally {
			lock.unlock();
		}
	}

	public boolean removeAll(Collection<?> c) {

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			return snapshot.removeAll(c);
		} finally {
			lock.unlock();
		}
	}

	public ArrayList<T> getSnapshot() {
		return snapshot;
	}

	public ReentrantLock getReentrantLock() {
		return lock;
	}

}
