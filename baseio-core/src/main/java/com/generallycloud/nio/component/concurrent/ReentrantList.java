package com.generallycloud.nio.component.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 仅适用于：</BR>
 * M => PUT </BR>
 * M => REMOVE </BR>
 * M => GET </BR>
 * O => FOREACH
 *
 * @param <T>
 */
public class ReentrantList<T> {

	private List<T>		snapshot		= new ArrayList<T>();
	private List<Event>		modifList		= new ArrayList<Event>();
	private ReentrantLock	loack		= new ReentrantLock();
	private boolean		modifid		= false;

	public List<T> getSnapshot() {

		takeSnapshot();
		
		return snapshot;
	}
	
	public T get(int index){
		
		return getSnapshot().get(index);
	}

	private void takeSnapshot() {
		if (modifid) {
			ReentrantLock lock = this.loack;

			lock.lock();

			List<T> snapshot = this.snapshot;
			
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

		lock.unlock();
	}
	
	public void clear(){
		
		ReentrantLock lock = this.loack;

		lock.lock();

		this.modifList.clear();

		this.modifid = false;
		
		this.snapshot.clear();

		lock.unlock();
	}

	public ReentrantLock getReentrantLock(){
		return loack;
	}

	public int size() {
		
		takeSnapshot();
		
		return snapshot.size();
		
	}
	
	public boolean isEmpty(){
		return size() == 0;
	}
	
	class Event {
		T		value;
		boolean	isAdd;
	}

}
