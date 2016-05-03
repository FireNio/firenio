package com.gifisan.nio.jms.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class SUBConsumerQueue implements ConsumerQueue {

	private ReentrantLock	lock		= new ReentrantLock();
	private List<Consumer>	consumers	= new ArrayList<Consumer>(128);
	private Consumer[]		snapshot	= new Consumer[128];

	public int size() {
		return consumers.size();
	}

	public void offer(Consumer consumer) {

		ReentrantLock lock = this.lock;

		lock.lock();

		this.consumers.add(consumer);

		lock.unlock();
	}

	public void remove(Consumer consumer) {

		ReentrantLock lock = this.lock;

		lock.lock();

		this.consumers.remove(consumer);

		lock.unlock();
	}

	public Consumer[] snapshot() {

		ReentrantLock lock = this.lock;

		lock.lock();

		if (size() == 0) {
			return null;
		}

		//FIXME  clone ?
		snapshot = consumers.toArray(new Consumer[] {});

		lock.unlock();

		return snapshot;
	}

	public Consumer poll(long timeout) {
		return null;
	}

	public void remove(List<Consumer> consumers) {
		ReentrantLock lock = this.lock;

		lock.lock();

		for(Consumer consumer:consumers){
			
			this.consumers.remove(consumer);
		}

		lock.unlock();
	}
	
	
	
}