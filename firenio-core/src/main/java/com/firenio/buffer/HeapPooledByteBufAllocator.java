package com.firenio.buffer;

import com.firenio.common.Unsafe;

/**
 * @program: firenio
 * @description:
 * @author: wangkai
 * @create: 2020-06-18 15:17
 **/
public class HeapPooledByteBufAllocator extends PooledByteBufAllocator {

    private byte[] heapMemory;

    public HeapPooledByteBufAllocator(ByteBufAllocatorGroup group, int capacity) {
        super(group, capacity);
    }

    @Override
    protected void initMemory() {
        byte[] memory = this.heapMemory;
        if (memory == null || memory.length != getCapacity()) {
            this.heapMemory = new byte[getCapacity()];
        }
    }

    @Override
    protected void expansion_and_copy(ByteBuf buf, int alloc, int size) {
        int read_index  = buf.readIndex();
        int write_index = buf.writeIndex();
        int offset      = buf.offset();
        buf.produce(alloc, size);
        System.arraycopy(heapMemory, offset, heapMemory, buf.offset(), write_index);
        buf.readIndex(read_index).writeIndex(write_index);
    }

    @Override
    protected ByteBuf newByteBuf() {
        return new PooledHeapByteBuf(this, heapMemory);
    }

    @Override
    public long getAddress() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void freeMemory() {
        // FIXME 这里不free了，如果在次申请的时候大小和这次一致，则不再重新申请
        // this.memory = null;
    }

}
