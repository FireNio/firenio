package com.generallycloud.test.nio.buffer;

public class MemoryUnit {
	
	private MemoryUnit previous;
	
	private int index;
	
	private boolean using;
	
	private MemoryUnit next;
	
	public MemoryUnit(int index) {
		this.index = index;
	}

	public MemoryUnit getPrevious() {
		return previous;
	}

	public void setPrevious(MemoryUnit previous) {
		this.previous = previous;
	}

	public int getIndex() {
		return index;
	}

	public MemoryUnit getNext() {
		return next;
	}

	public void setNext(MemoryUnit next) {
		this.next = next;
	}

	public boolean isUsing() {
		return using;
	}

	public void setUsing(boolean using) {
		this.using = using;
	}
}
