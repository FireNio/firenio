package com.generallycloud.nio.buffer;


@Deprecated
public class MemoryUnitV1 {

	private MemoryUnitV1	previous;

	private int			index;

	private boolean		using;

	private MemoryUnitV1	next;

	public MemoryUnitV1(int index) {
		this.index = index;
	}

	public MemoryUnitV1 getPrevious() {
		return previous;
	}

	public void setPrevious(MemoryUnitV1 previous) {
		this.previous = previous;
	}

	public int getIndex() {
		return index;
	}

	public MemoryUnitV1 getNext() {
		return next;
	}

	public void setNext(MemoryUnitV1 next) {
		this.next = next;
	}

	public boolean isUsing() {
		return using;
	}

	public void setUsing(boolean using) {
		this.using = using;
	}

	protected MemoryUnitV1() {

	}
	
	public String toString() {
		return "[index:"+ index + ",using:"+using+"]";
	}

}
