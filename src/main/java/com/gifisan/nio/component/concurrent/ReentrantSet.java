package com.gifisan.nio.component.concurrent;

import java.util.concurrent.locks.ReentrantLock;

public class ReentrantSet<K> {

	private static final byte[]		V	= {};

	private ReentrantMap<K, byte[]>	keys	= new ReentrantMap<K, byte[]>();

	public void add(K key) {

		keys.put(key, V);
	}

	public void remove(K key) {

		keys.remove(key);
	}

	public boolean contains(K key) {

		keys.takeSnapshot();

		return keys.get(key) != null;
	}
	
	public ReentrantLock getReentrantLock() {
		return keys.getReentrantLock();
	}

}
