package com.yoocent.mtp.component;

public class LinkNodeQueue<T> {

	private Node<T> current;
	
	private Node<T> last;
	
	public LinkNodeQueue(){
		this.current = new Node<T>();
		this.last = current;
	}
	
	public synchronized void addNode(Node<T> node){
		this.last.setNext(node);
		this.last = node;
	}

	public synchronized Node<T> getNode() {
		Node<T> node = current.getNext();
		if (node == null) {
			return null;
		}
		Node<T> next = node.getNext();
		if (next == null) {
			this.last = this.current;
		}
		this.current.setNext(next);
		return node;
	}
	
}


