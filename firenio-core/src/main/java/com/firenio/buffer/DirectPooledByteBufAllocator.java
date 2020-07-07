package com.firenio.buffer;

import java.nio.ByteBuffer;

import com.firenio.common.Unsafe;

/**
 * @program: firenio
 * @description:
 * @author: wangkai
 * @create: 2020-06-18 15:17
 **/
public class DirectPooledByteBufAllocator extends PooledByteBufAllocator {

    private long       address;
    private ByteBuffer directMemory;

    public DirectPooledByteBufAllocator(ByteBufAllocatorGroup group, int capacity) {
        super(group, capacity);
    }

    @Override
    protected void initMemory() {
        if (Unsafe.UNSAFE_BUF_AVAILABLE) {
            this.address = Unsafe.allocate(getCapacity());
        } else {
            this.directMemory = Unsafe.allocateDirectByteBuffer(getCapacity());
            this.address = Unsafe.address(directMemory);
        }
    }

    @Override
    protected void expansion_and_copy(ByteBuf buf, int alloc, int size) {
        int read_index  = buf.readIndex();
        int write_index = buf.writeIndex();
        int offset      = buf.offset();
        buf.produce(alloc, size);
        Unsafe.copyMemory(address + offset, address + buf.offset(), write_index);
        buf.readIndex(read_index).writeIndex(write_index);
    }

    @Override
    protected ByteBuf newByteBuf() {
        if (Unsafe.UNSAFE_BUF_AVAILABLE) {
            return new PooledUnsafeByteBuf(this, address);
        } else {
            return new PooledDirectByteBuf(this, directMemory.duplicate());
        }
    }

    @Override
    public long getAddress() {
        return address;
    }

    @Override
    public void freeMemory() {
        if (Unsafe.UNSAFE_BUF_AVAILABLE) {
            Unsafe.free(address);
        } else {
            Unsafe.freeByteBuffer(directMemory);
        }
    }

}
