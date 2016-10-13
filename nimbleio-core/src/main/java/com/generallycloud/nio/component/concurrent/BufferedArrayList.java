package com.generallycloud.nio.component.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class BufferedArrayList<T> {

	private ReentrantLock	lock		= new ReentrantLock();

	private List<T>		list1	= new ArrayList<T>();

	private List<T>		list2	= new ArrayList<T>();
	
	private List<T>		buffer	= list1;
	
	public void safeAdd(T t) {

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			buffer.add(t);
			
		} finally {

			lock.unlock();
		}
	}
	
	public void add(T t) {

		buffer.add(t);
	}
	
	public ReentrantLock getReentrantLock(){
		return lock;
	}
	
	public List<T> getBuffer(){
		
		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			if (buffer == list1) {
				
				buffer = list2;
				
				buffer.clear();
				
				return list1;
			}else{
				
				buffer = list1;
				
				buffer.clear();
				
				return list2;
			}
			
		} finally {

			lock.unlock();
		}
	}
	
	public int getBufferSize(){
		return buffer == list1 ? list2.size() : list1.size();
	}
	
}
