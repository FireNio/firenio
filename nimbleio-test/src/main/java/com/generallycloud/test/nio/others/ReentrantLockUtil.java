package com.generallycloud.test.nio.others;

import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockUtil {
	
	private ReentrantLock lock = new ReentrantLock();

	public static void main(String[] args) {
		
	}
	
	void test(){
		
		ReentrantLock lock = this.lock;
		
		lock.lock();
		
		try {
			
		} finally {
			lock.unlock();
		}
		
	}
	
}
