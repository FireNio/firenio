package com.gifisan.nio.component.concurrent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantMap<K, V> {

	private Map<K, V>		snapshot	= new HashMap<K, V>();
	private List<Event>		modifList	= new ArrayList<Event>();
	private ReentrantLock	loack	= new ReentrantLock();
	private boolean		modifid	= false;
	private int			size;

	public V get(K key) {

		takeSnapshot();

		return snapshot.get(key);
	}

	public void takeSnapshot() {
		if (modifid) {
			ReentrantLock lock = this.loack;

			lock.lock();

			List<Event> modifList = this.modifList;

			for (Event e : modifList) {

				if (e.isAdd) {
					snapshot.put(e.key, e.value);
				} else {
					snapshot.remove(e.key);
				}
			}

			modifList.clear();

			this.modifid = false;

			lock.unlock();
		}
	}

	public Map<K, V> getSnapshot() {

		takeSnapshot();

		return snapshot;
	}

	public boolean put(K key, V value) {

		ReentrantLock lock = this.loack;

		lock.lock();

		Event event = new Event();

		event.key = key;
		event.value = value;
		event.isAdd = true;

		this.modifList.add(event);

		this.size++;

		this.modifid = true;

		lock.unlock();

		return true;
	}

	public void remove(K key) {

		ReentrantLock lock = this.loack;

		lock.lock();

		Event event = new Event();

		event.key = key;
		event.isAdd = false;

		this.modifList.add(event);

		this.size--;

		this.modifid = true;

		lock.unlock();
	}

	public ReentrantLock getReentrantLock() {
		return loack;
	}

	//FIXME you wen ti
	public int size() {
		return size;
	}

	class Event {
		K		key;
		V		value;
		boolean	isAdd;
	}

}
