package com.generallycloud.baseio.concurrent;

import com.generallycloud.baseio.common.UnsafeUtil;

public class ScmpLinkedQueue<V> extends ScspLinkedQueue<V> {

    private static final long tailOffset;

    static {
        try {
            tailOffset = UnsafeUtil
                    .objectFieldOffset(ScspLinkedQueue.class.getDeclaredField("tail"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public void offer(V v) {
        Node<V> n = new Node<V>(v);
        for (;;) {
            Node<V> tail = this.getTail();
            if (UnsafeUtil.compareAndSwapObject(this, tailOffset, tail, n)) {
                tail.next = n;
                incrementAndGet();
                return;
            }
        }
    }

}
