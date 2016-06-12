package com.gifisan.nio.concurrent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

//FIXME 做重复判断
public class ReentrantList<T> {

	private List<T>		snapshot		= new ArrayList<T>();
	private List<T>		addList		= new ArrayList<T>();
	private List<T>		removeList	= new ArrayList<T>();
	private ReentrantLock	loack		= new ReentrantLock();
	private Set<Object>		dataSet		= new HashSet<Object>();
	private boolean		addFlag		= false;
	private boolean		removeFlag	= false;
	private int			size			= 0;

	public List<T> getSnapshot() {

		takeSnapshot();
		
		return snapshot;
	}

	private void takeSnapshot() {
		if (addFlag) {
			ReentrantLock lock = this.loack;

			lock.lock();

			List<T> addList = this.addList;

			for (T t : addList) {
				snapshot.add(t);
			}

			addList.clear();

			this.addFlag = false;

			lock.unlock();
		}

		if (removeFlag) {

			ReentrantLock lock = this.loack;

			lock.lock();

			List<T> removeList = this.removeList;

			for (T t : removeList) {
				snapshot.remove(t);
				dataSet.remove(t);
			}

			removeList.clear();

			this.removeFlag = false;

			lock.unlock();
		}
	}

	public void addAll(List<T> ts) {

		ReentrantLock lock = this.loack;

		lock.lock();

		for (T t : ts) {
			if (!dataSet.contains(t)) {

				add0(t);
			}
		}

		lock.unlock();

	}

	private void add0(T t) {

		this.dataSet.add(t);

		this.addList.add(t);

		this.addFlag = true;

		this.size++;
	}

	public boolean add(T t) {

		ReentrantLock lock = this.loack;

		lock.lock();

		if (dataSet.contains(t)) {

			lock.unlock();

			return false;
		}

		this.add0(t);

		lock.unlock();

		return true;
	}

	public void remove(T t) {

		ReentrantLock lock = this.loack;

		lock.lock();

		if (!dataSet.contains(t)) {

			lock.unlock();

			return;
		}

		this.remove0(t);

		lock.unlock();
	}

	private void remove0(T t) {

		this.removeList.add(t);

		this.removeFlag = true;

		this.size--;
	}

	public void removeAll(List<T> ts) {

		ReentrantLock lock = this.loack;

		lock.lock();

		for (T t : ts) {
			if (dataSet.contains(t)) {

				this.remove0(t);
			}
		}

		lock.unlock();
	}
	
	public ReentrantLock getReentrantLock(){
		return loack;
	}

	public int size() {
		return size;
	}

}
