package com.gifisan.mtp.component;

import java.util.concurrent.CountDownLatch;

public class Node<T> {
	
	private CountDownLatch countDownLatch = new CountDownLatch(1);

	private Node<T> next;
	
	private T value;

	Node<T> getNext() {
		return next;
	}

	void setNext(Node<T> next) {
		this.next = next;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}
	
	public void latchWait() {
		try {
			this.countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void wakeup(){
		
		this.countDownLatch.countDown();
	}
	
}