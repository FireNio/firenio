package com.yoocent.mtp.component;

public class NLinkNodeQueue<T> {

	private Node<T> current;
	
	private Node<T> last;
	
	public NLinkNodeQueue(){
		this.current = new Node<T>();
		this.last = current;
	}
	
	public void addNode(Node<T> node){
		this.last.setNext(node);
		this.last = node;
	}

	public Node<T> getNode() {
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


