package com.generallycloud.baseio.concurrent;

import com.generallycloud.baseio.common.UnsafeUtil;
import com.generallycloud.baseio.protocol.AbstractFrame;

public class CwfScmpLinkedQueueUnsafe<T> extends ScmpLinkedQueueUnsafe<T> {

    public CwfScmpLinkedQueueUnsafe(Linkable linkable) {
        super(linkable, nextOffset);
    }

    private static final long nextOffset;
    static {
        try {
            Class<?> k = AbstractFrame.class;
            nextOffset = UnsafeUtil.objectFieldOffset(k.getDeclaredField("next"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

}
