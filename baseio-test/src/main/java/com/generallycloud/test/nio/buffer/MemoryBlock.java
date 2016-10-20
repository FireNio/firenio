package com.generallycloud.test.nio.buffer;

public class MemoryBlock {
	
	private MemoryBlock previous;
	
	private MemoryBlock next;

	private MemoryUnit start;
	
	private int size;
	
	private MemoryUnit end;
	
	public void setMemory(MemoryUnit start,MemoryUnit end) {
		this.start = start;
		this.end = end;
		this.size = end.getIndex() - start.getIndex() + 1;
	}

	public MemoryUnit getStart() {
		return start;
	}

	public int getSize() {
		return size;
	}

	public MemoryUnit getEnd() {
		return end;
	}

	public MemoryBlock getPrevious() {
		return previous;
	}

	public void setPrevious(MemoryBlock previous) {
		this.previous = previous;
	}

	public MemoryBlock getNext() {
		return next;
	}

	public void setNext(MemoryBlock next) {
		this.next = next;
	}
	
	public MemoryBlock use(){
		this.start.setUsing(true);
		this.end.setUsing(true);
		return this;
	}
	
	public void free(){
		this.start.setUsing(false);
		this.end.setUsing(false);
	}
	
	public boolean using(){
		return this.start.isUsing();
	}
	
}
