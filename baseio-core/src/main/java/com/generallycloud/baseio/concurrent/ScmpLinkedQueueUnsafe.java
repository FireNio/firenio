package com.generallycloud.baseio.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.baseio.common.UnsafeUtil;

public class ScmpLinkedQueueUnsafe<T> implements LinkedQueue<T> {

    protected Linkable      head = null;               // volatile ?
    protected AtomicInteger size = new AtomicInteger();
    protected Linkable      tail = null;               // volatile ?
    protected final long    nextOffset;

    public ScmpLinkedQueueUnsafe(Linkable linkable, long nextOffset) {
        linkable.setValidate(false);
        this.head = linkable;
        this.tail = linkable;
        this.nextOffset = nextOffset;
    }

    @SuppressWarnings("unchecked")
    private T get(Linkable h) {
        if (h.isValidate()) {
            Linkable next = h.getNext();
            if (next == null) {
                h.setValidate(false);
            } else {
                head = next;
            }
            this.size.decrementAndGet();
            return (T) h;
        } else {
            Linkable next = h.getNext();
            head = next;
            return get(next);
        }
    }

    @Override
    public void offer(Linkable linkable) {
        for (;;) {
            //FIXME 设置next后，设置tail
            if (UnsafeUtil.compareAndSwapObject(tail, nextOffset, null, linkable)) {
                tail = linkable;
                size.incrementAndGet();
                return;
            }
        }
    }

    @Override
    public T poll() {
        int size = size();
        if (size == 0) {
            return null;
        }
        return get(head);
    }

    @Override
    public int size() {
        return size.get();
    }

}
