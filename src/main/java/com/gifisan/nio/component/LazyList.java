package com.gifisan.nio.component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class LazyList<T> {

	private List<T> data = new ArrayList<T>();
	
	private List<T> addList = new ArrayList<T>();
	
	private List<T> removeList = new ArrayList<T>();
	
	private boolean addFlag = false;
	
	private boolean removeFlag = false;
	
	private ReentrantLock addLock = new ReentrantLock();
	
	private ReentrantLock removeLock = new ReentrantLock();
	
	public List<T> getData(){
		
		if (addFlag) {
			ReentrantLock lock = this.addLock;
			
			lock.lock();
			
			List<T> addList = this.addList;
			
			for(T t:addList){
				data.add(t);
			}
			
			addList.clear();
			
			this.addFlag = false;
			
			lock.unlock();
		}

		if (removeFlag) {
			
			ReentrantLock lock = this.removeLock;
			
			lock.lock();
			
			List<T> removeList = this.removeList;
			
			for(T t:removeList){
				data.remove(t);
			}
			
			removeList.clear();
			
			this.removeFlag = false;
			
			lock.unlock();
		}
		
		
		return data;
	}
	
	public void addAll(List<T> ts){
		
		ReentrantLock lock = this.addLock;
		
		lock.lock();
		
		this.addList.addAll(ts);
		
		this.addFlag = true;
		
		lock.unlock();
	}
	
	
	public void add(T t){
		
		ReentrantLock lock = this.addLock;
		
		lock.lock();
		
		this.addList.add(t);
		
		this.addFlag = true;
		
		lock.unlock();
	}
	
	public void remove(T t){
		
		ReentrantLock lock = this.removeLock;
		
		lock.lock();
		
		this.removeList.add(t);
		
		this.removeFlag = true;
		
		lock.unlock();
	}
	
	public void removeAll(List<T> ts){
		
		ReentrantLock lock = this.removeLock;
		
		lock.lock();
		
		this.removeList.removeAll(ts);
		
		this.removeFlag = true;
		
		lock.unlock();
	}
	
	public int size(){
		return data.size();
	}
	
}
