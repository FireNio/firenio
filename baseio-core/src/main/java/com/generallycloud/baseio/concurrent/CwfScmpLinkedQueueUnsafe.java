package com.generallycloud.baseio.concurrent;

import com.generallycloud.baseio.common.UnsafeUtil;
import com.generallycloud.baseio.protocol.ChannelWriteFutureImpl;

public class CwfScmpLinkedQueueUnsafe<T extends Linkable<T>> extends ScmpLinkedQueueUnsafe<T> {

	public CwfScmpLinkedQueueUnsafe(Linkable<T> linkable) {
		super(linkable, nextOffset);
	}

	private static final long nextOffset;
	static {
		try {
			Class<?> k = ChannelWriteFutureImpl.class;
			nextOffset = UnsafeUtil.objectFieldOffset(k.getDeclaredField("next"));
		} catch (Exception e) {
			throw new Error(e);
		}
	}

}
