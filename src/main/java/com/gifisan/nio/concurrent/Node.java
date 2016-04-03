package com.gifisan.nio.concurrent;

public class Node<T> {
	
	public Node(){}
	
	public Node(T value) {
		this.value = value;
	}

	private Node<T> next;
	
	private Node<T> previous;
	
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

	public Node<T> getPrevious() {
		return previous;
	}

	public void setPrevious(Node<T> previous) {
		this.previous = previous;
	}
	
	
	
}