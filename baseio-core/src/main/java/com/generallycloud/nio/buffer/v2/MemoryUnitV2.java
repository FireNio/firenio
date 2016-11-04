package com.generallycloud.nio.buffer.v2;


@Deprecated
public class MemoryUnitV2 {

	private MemoryUnitV2	previous;

	private int			index;

	private boolean		using;

	private MemoryUnitV2	next;

	public MemoryUnitV2(int index) {
		this.index = index;
	}

	public MemoryUnitV2 getPrevious() {
		return previous;
	}

	public void setPrevious(MemoryUnitV2 previous) {
		this.previous = previous;
	}

	public int getIndex() {
		return index;
	}

	public MemoryUnitV2 getNext() {
		return next;
	}

	public void setNext(MemoryUnitV2 next) {
		this.next = next;
	}

	public boolean isUsing() {
		return using;
	}

	public void setUsing(boolean using) {
		this.using = using;
	}

	protected MemoryUnitV2() {

	}
	
	public String toString() {
		return "[index:"+ index + ",using:"+using+"]";
	}

}
