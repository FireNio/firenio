package com.gifisan.nio.concurrent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.gifisan.nio.component.EntryList;

public class ReentrantMap<K,V> {

	private Map<K, V>		snapshot		= new HashMap<K, V>();
	private EntryList<K, V>	addList		= new EntryList<K, V>();
	private List<K>		removeList	= new ArrayList<K>();
	private ReentrantLock	loack		= new ReentrantLock();
	private boolean		addFlag		= false;
	private boolean		removeFlag	= false;
	private int			size			= 0;

	public V get(K key) {

		takeSnapshot();

		return snapshot.get(key);
	}

	public void takeSnapshot() {
		if (addFlag) {
			ReentrantLock lock = this.loack;

			lock.lock();

			EntryList<K, V> addList = this.addList;

			for (int i = 0, size = addList.size(); i < size; i++) {

				snapshot.put(addList.getKey(i), addList.getValue(i));
			}

			addList.clear();

			this.addFlag = false;

			lock.unlock();
		}

		if (removeFlag) {

			ReentrantLock lock = this.loack;

			lock.lock();

			List<K> removeList = this.removeList;

			for (K key : removeList) {
				snapshot.remove(key);
			}

			removeList.clear();

			this.removeFlag = false;

			lock.unlock();
		}
	}

	public boolean put(K key, V value) {

		ReentrantLock lock = this.loack;

		lock.lock();

		this.addList.add(key, value);

		this.size++;

		this.addFlag = true;

		lock.unlock();

		return true;
	}

	public void remove(K key) {

		ReentrantLock lock = this.loack;

		lock.lock();

		this.removeList.add(key);

		this.size--;

		this.removeFlag = true;

		lock.unlock();
	}

	public ReentrantLock getReentrantLock() {
		return loack;
	}

	public int size() {
		return size;
	}

}
