package com.gifisan.mtp.concurrent;

public class Node<T> {
	
	public Node(){}
	
	public Node(T value) {
		this.value = value;
	}

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