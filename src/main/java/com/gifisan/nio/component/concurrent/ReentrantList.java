package com.gifisan.nio.component.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

//FIXME 是否需要做重复判断
public class ReentrantList<T> {

	private List<T>		snapshot		= new ArrayList<T>();
	private List<Event>		modifList		= new ArrayList<Event>();
	private ReentrantLock	loack		= new ReentrantLock();
	private boolean		modifid		= false;
	private int			size			;

	public List<T> getSnapshot() {

		takeSnapshot();
		
		return snapshot;
	}

	private void takeSnapshot() {
		if (modifid) {
			ReentrantLock lock = this.loack;

			lock.lock();

			List<Event> modifList = this.modifList;

			for (Event e : modifList) {
				
				if (e.isAdd) {
					snapshot.add(e.value);
				}else{
					snapshot.remove(e.value);
				}
			}
			
			modifList.clear();

			this.modifid = false;

			lock.unlock();
		}
	}

	public boolean add(T t) {

		ReentrantLock lock = this.loack;

		lock.lock();

		Event e = new Event();
		e.isAdd = true;
		e.value = t;

		this.modifList.add(e);

		this.modifid = true;

		this.size++;

		lock.unlock();

		return true;
	}

	public void remove(T t) {

		ReentrantLock lock = this.loack;

		lock.lock();

		Event e = new Event();
		e.isAdd = false;
		e.value = t;

		this.modifList.add(e);

		this.modifid = true;

		this.size--;

		lock.unlock();
	}

	public ReentrantLock getReentrantLock(){
		return loack;
	}

	public int size() {
		return size;
	}
	
	class Event {
		T		value;
		boolean	isAdd;
	}

}
