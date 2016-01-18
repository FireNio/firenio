package com.gifisan.mtp.concurrent;



/**
 * 仅适用于一个线程offer多个线程poll的场景
 *
 * @param <T>
 */
public class LinkNodeQueue<T> implements Queue<T>{
	
	public LinkNodeQueue(){
		this.current = new Node();
		this.last = current;
	}

	private Node<T> current;
	
	private Node<T> last ;
	
	public void offer(T t){
		Node<T> node = new Node<T>(t);
		last.setNext(node);
		last = node;
	}
	
	public boolean empty() {
		return current.getValue() == null;
	}

	public T poll() {
//		final ReentrantLock _lock = this.lock; 
//		_lock.lock();
//		try {
		synchronized(this){
			Node<T> next = current.getNext();
			
			if (next == null) {
				if (current.getValue() == null) {
					return null;
				}
				T t = current.getValue();
				current.setValue(null);
				return t;
			}
			
			Node<T> node = current;
			
			current = next;
			
			return node.getValue();
		}
//		} finally {
//			lock.unlock();
//		}
	}
}


