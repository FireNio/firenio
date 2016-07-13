package com.gifisan.nio.component.concurrent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantMap<K, V> {

	private Map<K, V>		snapshot	= new HashMap<K, V>();
	private ReentrantLock	loack	= new ReentrantLock();

	public V get(K key) {

		return snapshot.get(key);
	}

	public Map<K, V> getSnapshot() {

		return snapshot;
	}

	public V put(K key, V value) {

		ReentrantLock lock = this.loack;

		lock.lock();

		try {
			
			return snapshot.put(key, value);
			
		} finally {
			lock.unlock();
		}
	}

	public V remove(K key) {

		ReentrantLock lock = this.loack;

		lock.lock();

		try {
			
			return snapshot.remove(key);
			
		} finally {
			lock.unlock();
		}
	}

	public ReentrantLock getReentrantLock() {
		return loack;
	}

	public int size() {
		
		return snapshot.size();
	}
	
	public void clear(){
		
		ReentrantLock lock = this.loack;

		lock.lock();

		this.snapshot.clear();

		lock.unlock();
	}
	
	public boolean isEmpty(){
		return snapshot.isEmpty();
	}
}
