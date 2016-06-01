package com.gifisan.nio.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantMap {

	private Map<Object, MapAble>	snapshot		= new HashMap<Object, MapAble>();
	private List<MapAble>		addList		= new ArrayList<MapAble>();
	private List<Object>		removeList	= new ArrayList<Object>();
	private ReentrantLock		loack		= new ReentrantLock();
	private boolean			addFlag		= false;
	private boolean			removeFlag	= false;
	private int				size			= 0;

	public MapAble getValue(Object key) {

		takeSnapshot();

		return snapshot.get(key);
	}

	public void takeSnapshot() {
		if (addFlag) {
			ReentrantLock lock = this.loack;

			lock.lock();

			List<MapAble> addList = this.addList;

			for (MapAble t : addList) {
				snapshot.put(t.getKey(), t);
			}

			addList.clear();

			this.addFlag = false;

			lock.unlock();
		}

		if (removeFlag) {

			ReentrantLock lock = this.loack;

			lock.lock();

			List<Object> removeList = this.removeList;

			for (Object t : removeList) {
				snapshot.remove(t);
			}

			removeList.clear();

			this.removeFlag = false;

			lock.unlock();
		}
	}

	public void addAll(List<MapAble> ts) {

		ReentrantLock lock = this.loack;

		lock.lock();

		for (MapAble t : ts) {

			add0(t);
		}

		this.addFlag = true;

		lock.unlock();

	}

	private void add0(MapAble t) {

		this.addList.add(t);

		this.size++;
	}

	public boolean add(MapAble t) {

		ReentrantLock lock = this.loack;

		lock.lock();

		this.add0(t);
		
		this.addFlag = true;

		lock.unlock();

		return true;
	}

	public void remove(Object t) {

		ReentrantLock lock = this.loack;

		lock.lock();

		this.remove0(t);

		this.removeFlag = true;
		
		lock.unlock();
	}

	private void remove0(Object t) {

		this.removeList.add(t);

		this.size--;
	}

	public void removeAll(List<Object> ts) {

		ReentrantLock lock = this.loack;

		lock.lock();

		for (Object t : ts) {

			this.remove0(t);
			
			this.removeFlag = true;
		}

		lock.unlock();
	}

	public ReentrantLock getReentrantLock() {
		return loack;
	}

	public int size() {
		return size;
	}
	
}
