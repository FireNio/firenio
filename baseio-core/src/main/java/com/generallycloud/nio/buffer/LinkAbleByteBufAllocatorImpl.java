package com.generallycloud.nio.buffer;

import com.generallycloud.nio.AbstractLifeCycle;
import com.generallycloud.nio.Linkable;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.ReleaseUtil;

public class LinkAbleByteBufAllocatorImpl extends AbstractLifeCycle implements LinkAbleByteBufAllocator {

	private Linkable<LinkAbleByteBufAllocator>	next;

	private int							index;

	private ByteBufAllocator					allocator;

	private MCByteBufAllocator				mcByteBufAllocator;

	public LinkAbleByteBufAllocatorImpl(MCByteBufAllocator mcByteBufAllocator,ByteBufAllocator allocator, int index) {
		this.index = index;
		this.allocator = allocator;
		this.mcByteBufAllocator = mcByteBufAllocator;
	}

	public Linkable<LinkAbleByteBufAllocator> getNext() {
		return next;
	}

	public void setNext(Linkable<LinkAbleByteBufAllocator> next) {
		this.next = next;
	}

	public int getIndex() {
		return index;
	}

	public LinkAbleByteBufAllocator getValue() {
		return this;
	}

	public ByteBufAllocator unwrap() {
		return allocator;
	}

	public void release(ByteBuf buf) {
		ReleaseUtil.release(buf);
	}

	public ByteBuf allocate(int capacity) {

		ByteBuf buf = unwrap().allocate(capacity);

		if (buf == null) {
			buf = mcByteBufAllocator.allocate(capacity, this);
		}

		return buf;
	}

	public int getUnitMemorySize() {
		return unwrap().getUnitMemorySize();
	}

	public void freeMemory() {
		unwrap().freeMemory();
	}

	public int getCapacity() {
		return unwrap().getCapacity();
	}

	protected void doStart() throws Exception {
		unwrap().start();
	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(unwrap());
	}

	protected boolean logger() {
		return false;
	}
	
	public String toString() {
		return unwrap().toString();
	}
	
}
