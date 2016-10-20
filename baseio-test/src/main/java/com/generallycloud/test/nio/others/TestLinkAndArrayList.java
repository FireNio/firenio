package com.generallycloud.test.nio.others;

import com.generallycloud.nio.common.test.ITest;
import com.generallycloud.nio.common.test.ITestHandle;

public class TestLinkAndArrayList {

	public static void main(String[] args) {
		
		int size = 2999 * 10000;
		
		testArrayList(size);
//		testLinkedList(size);
		
	}
	

	
	static void testLinkedList(int size){
		
		Node n = new Node();
		final Node r = n;
		for (int i = 0; i < size; i++) {
			Node t = new Node();
			n.setNext(t);
			n = t;
		}
		
		ITestHandle.doTest(new ITest() {
			
			public void test(int i1) throws Exception {
				Node t = r;
				for(;;){
					t.hello();
					t = t.getNext();
					if (t == null) {
						break;
					}
				}
				
			}
		}, 1, "TestLinkedList");
		
	}
	
	
	static void testArrayList(int size){
		
		final Node []ns = new Node[size];
		
		for (int i = 0; i < ns.length; i++) {
			ns[i] = new Node();
		}
		
		ITestHandle.doTest(new ITest() {
			
			public void test(int i1) throws Exception {
				ns[i1].hello();
			}
		}, size, "TestArrayList");
		
	}
	
}

class Node{
	
	private Node next;
	
	public Node getNext() {
		return next;
	}

	public void setNext(Node next) {
		this.next = next;
	}

	public void hello(){
		
	}
}
