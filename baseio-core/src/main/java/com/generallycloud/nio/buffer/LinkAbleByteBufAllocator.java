package com.generallycloud.nio.buffer;

import com.generallycloud.nio.Linkable;

public interface LinkAbleByteBufAllocator extends ByteBufAllocator, Linkable<LinkAbleByteBufAllocator> {

	public abstract int getIndex();

	public abstract ByteBufAllocator unwrap();
}
