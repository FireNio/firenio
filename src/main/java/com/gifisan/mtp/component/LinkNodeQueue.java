package com.gifisan.mtp.component;

import java.util.concurrent.locks.ReentrantLock;

public class LinkNodeQueue<T> {

	private ReentrantLock lock = new ReentrantLock();
	
	private Node<T> current;
	
	private Node<T> last ;
	
	public void addNode(Node<T> node){
		final ReentrantLock _lock = this.lock; 
		_lock.lock();
		try {
			if (current == null) {
				current = node;
				last = current;
			}else{
				last.setNext(node);
				last = node;
			}
		} finally {
			lock.unlock();
		}
	}

	public Node<T> getNode() {
		final ReentrantLock _lock = this.lock; 
		_lock.lock();
		try {
			if (current == null) {
				return null;
			}
			
			Node<T> node = current;
			
			current = node.getNext();
			
			return node;
		} finally {
			lock.unlock();
		}
	}
	
}


