package com.generallycloud.nio.component.concurrent;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 仅适用于：</BR>
 * M => PUT </BR>
 * M => REMOVE </BR>
 * O => GET </BR>
 * O => FOREACH
 *
 * @param <K>
 */
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

		return keys.get(key) != null;
	}
	
	public ReentrantLock getReentrantLock() {
		return keys.getReentrantLock();
	}

}
