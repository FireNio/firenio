package com.yoocent.mtp.component;

public class Node<T> {

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
	
}