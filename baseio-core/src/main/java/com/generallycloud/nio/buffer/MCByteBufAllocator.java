package com.generallycloud.nio.buffer;

import com.generallycloud.nio.AbstractLifeCycle;
import com.generallycloud.nio.Linkable;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.configuration.ServerConfiguration;

public class MCByteBufAllocator extends AbstractLifeCycle {

	private LinkAbleByteBufAllocator[]	allocators	= null;

	private LinkAbleByteBufAllocator	allocator		= null;
	
	private int cycle;

	public MCByteBufAllocator(BaseContext context) {
		createLinkAbleByteBufAllocator(context);
	}

	public ByteBuf allocate(int capacity, LinkAbleByteBufAllocator allocator) {

		int cycle = this.cycle;
		
		for (int i = 0; i < cycle; i++) {
			
			LinkAbleByteBufAllocator temp = allocator.getNext().getValue();
			
			ByteBuf buf = temp.unwrap().allocate(capacity);
			
			if (buf == null) {
				
				allocator = temp;
				
				continue;
			}
			
			return buf;
		}
		
		return UnpooledByteBufAllocator.allocate(capacity);
		
	}

	private void createLinkAbleByteBufAllocator(BaseContext context) {

		ServerConfiguration c = context.getServerConfiguration();

		int core = c.getSERVER_CORE_SIZE();

		int capacity = c.getSERVER_MEMORY_POOL_CAPACITY();

		int unitMemorySize = c.getSERVER_MEMORY_POOL_UNIT();

		boolean direct = c.isSERVER_MEMORY_POOL_DIRECT();

		this.allocators = new LinkAbleByteBufAllocator[core];

		for (int i = 0; i < allocators.length; i++) {

			ByteBufAllocator allocator = new SimpleByteBufAllocator(capacity, unitMemorySize, direct);

			allocators[i] = new LinkAbleByteBufAllocatorImpl(this,allocator, i);
		}
		
		cycle = core -1;
	}

	protected void doStart() throws Exception {
		LinkAbleByteBufAllocator first = null;
		LinkAbleByteBufAllocator last = null;

		for (int i = 0; i < allocators.length; i++) {

			LinkAbleByteBufAllocator allocator = allocators[i];

			allocator.start();

			if (first == null) {
				first = allocator;
				last = allocator;
				continue;
			}

			last.setNext(allocator);

			last = allocator;
		}

		last.setNext(first);

		this.allocator = first;
	}

	protected void doStop() throws Exception {

		for (LinkAbleByteBufAllocator allocator : allocators) {

			if (allocator == null) {
				continue;
			}

			LifeCycleUtil.stop(allocator);
		}

		this.allocator = null;
	}

	public ByteBufAllocator getNextBufAllocator() {

		Linkable<LinkAbleByteBufAllocator> next = this.allocator.getNext();

		LinkAbleByteBufAllocator value = next.getValue();

		this.allocator = value;

		return value;
	}
	
	public String toDebugString(){
		StringBuilder builder = new StringBuilder();
		for (ByteBufAllocator allocator :allocators) {
			builder.append("\n</BR>");
			builder.append(allocator.toString());
		}
		return builder.toString();
	}

}
