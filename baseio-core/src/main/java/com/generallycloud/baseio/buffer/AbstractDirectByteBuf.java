/*
 * Copyright 2015-2017 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.generallycloud.baseio.buffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.generallycloud.baseio.common.UnsafeUtil;

public abstract class AbstractDirectByteBuf extends AbstractByteBuf {

    protected ByteBuffer memory;

    public AbstractDirectByteBuf(ByteBufAllocator allocator, ByteBuffer memory) {
        super(allocator);
        this.memory = memory;
    }

    @Override
    public int absLimit() {
        return memory.limit();
    }

    @Override
    public int absPos() {
        return memory.position();
    }

    @Override
    public byte[] array() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ByteBuf clear() {
        memory.position(offset).limit(ix(capacity));
        return this;
    }

    @Override
    public ByteBuf flip() {
        memory.limit(memory.position());
        memory.position(offset);
        return this;
    }

    @Override
    public int forEachByte(int index, int length, ByteProcessor processor) {
        int start = ix(index);
        int end = start + length;
        try {
            for (int i = start; i < end; i++) {
                if (!processor.process(getByte(i))) {
                    return i - start;
                }
            }
        } catch (Exception e) {}
        return -1;
    }

    @Override
    public int forEachByteDesc(int index, int length, ByteProcessor processor) {
        int start = ix(index);
        int end = start + length;
        try {
            for (int i = end; i >= start; i--) {
                if (!processor.process(getByte(i))) {
                    return i - start;
                }
            }
        } catch (Exception e) {}
        return -1;
    }

    @Override
    public void get(byte[] dst, int offset, int length) {
        memory.get(dst, offset, length);
    }

    @Override
    public byte getByte() {
        return memory.get();
    }

    @Override
    public byte getByte(int index) {
        return memory.get(ix(index));
    }

    @Override
    public int getInt() {
        return memory.getInt();
    }

    @Override
    public int getInt(int index) {
        return memory.getInt(ix(index));
    }

    @Override
    public int getIntLE() {
        memory.order(ByteOrder.LITTLE_ENDIAN);
        int v = memory.getInt();
        memory.order(ByteOrder.BIG_ENDIAN);
        return v;
    }

    @Override
    public int getIntLE(int index) {
        memory.order(ByteOrder.LITTLE_ENDIAN);
        int v = memory.getInt(ix(index));
        memory.order(ByteOrder.BIG_ENDIAN);
        return v;
    }

    @Override
    public long getLong() {
        return memory.getLong();
    }

    @Override
    public long getLong(int index) {
        return memory.getLong(ix(index));
    }

    @Override
    public long getLongLE() {
        memory.order(ByteOrder.LITTLE_ENDIAN);
        long v = memory.getLong();
        memory.order(ByteOrder.BIG_ENDIAN);
        return v;
    }

    @Override
    public long getLongLE(int index) {
        memory.order(ByteOrder.LITTLE_ENDIAN);
        long v = memory.getLong(ix(index));
        memory.order(ByteOrder.BIG_ENDIAN);
        return v;
    }

    @Override
    public ByteBuffer getNioBuffer() {
        return memory;
    }

    @Override
    public short getShort() {
        return memory.getShort();
    }

    @Override
    public short getShort(int index) {
        return memory.getShort(ix(index));
    }

    @Override
    public short getShortLE() {
        memory.order(ByteOrder.LITTLE_ENDIAN);
        short v = memory.getShort();
        memory.order(ByteOrder.BIG_ENDIAN);
        return v;
    }

    @Override
    public short getShortLE(int index) {
        memory.order(ByteOrder.LITTLE_ENDIAN);
        short v = memory.getShort(ix(index));
        memory.order(ByteOrder.BIG_ENDIAN);
        return v;
    }

    @Override
    public short getUnsignedByte() {
        return (short) (getByte() & 0xff);
    }

    @Override
    public short getUnsignedByte(int index) {
        return (short) (getByte(index) & 0xff);
    }

    @Override
    public long getUnsignedInt() {
        long v = toUnsignedInt(memory.getInt());
        return v;
    }

    @Override
    public long getUnsignedInt(int index) {
        return toUnsignedInt(memory.getInt(ix(index)));
    }

    @Override
    public long getUnsignedIntLE() {
        memory.order(ByteOrder.LITTLE_ENDIAN);
        long v = toUnsignedInt(memory.getInt());
        memory.order(ByteOrder.BIG_ENDIAN);
        return v;
    }

    @Override
    public long getUnsignedIntLE(int index) {
        memory.order(ByteOrder.LITTLE_ENDIAN);
        long v = toUnsignedInt(memory.getInt(ix(index)));
        memory.order(ByteOrder.BIG_ENDIAN);
        return v;
    }

    @Override
    public int getUnsignedShort() {
        return memory.getShort() & 0xffff;
    }

    @Override
    public int getUnsignedShort(int index) {
        return memory.getShort(ix(index)) & 0xffff;
    }

    @Override
    public int getUnsignedShortLE() {
        memory.order(ByteOrder.LITTLE_ENDIAN);
        int v = memory.getShort() & 0xffff;
        memory.order(ByteOrder.BIG_ENDIAN);
        return v;
    }

    @Override
    public int getUnsignedShortLE(int index) {
        memory.order(ByteOrder.LITTLE_ENDIAN);
        int v = memory.getShort(ix(index)) & 0xff;
        memory.order(ByteOrder.BIG_ENDIAN);
        return v;
    }

    @Override
    public boolean hasArray() {
        return false;
    }

    @Override
    public boolean hasRemaining() {
        return memory.hasRemaining();
    }

    @Override
    public int limit() {
        return memory.limit() - offset;
    }

    @Override
    public ByteBuf limit(int limit) {
        memory.limit(ix(limit));
        return this;
    }

    @Override
    public ByteBuf markL() {
        markLimit = memory.limit();
        return this;
    }

    @Override
    public ByteBuf markP() {
        memory.mark();
        return this;
    }

    @Override
    public ByteBuffer nioBuffer() {
        return memory;
    }

    @Override
    public int position() {
        return memory.position() - offset;
    }

    @Override
    public ByteBuf position(int position) {
        memory.position(ix(position));
        return this;
    }

    @Override
    public void put(byte[] src, int offset, int length) {
        memory.put(src, offset, length);
    }

    @Override
    public void putByte(byte b) {
        memory.put(b);
    }

    @Override
    public void putInt(int value) {
        memory.putInt(value);
    }

    @Override
    public void putIntLE(int value) {
        memory.order(ByteOrder.LITTLE_ENDIAN);
        memory.putInt(value);
        memory.order(ByteOrder.BIG_ENDIAN);
    }

    @Override
    public void putLong(long value) {
        memory.putLong(value);
    }

    @Override
    public void putLongLE(long value) {
        memory.order(ByteOrder.LITTLE_ENDIAN);
        memory.putLong(value);
        memory.order(ByteOrder.BIG_ENDIAN);
    }

    @Override
    public void putShort(short value) {
        memory.putShort(value);
    }

    @Override
    public void putShortLE(short value) {
        memory.order(ByteOrder.LITTLE_ENDIAN);
        memory.putShort(value);
        memory.order(ByteOrder.BIG_ENDIAN);
    }

    @Override
    public void putUnsignedInt(long value) {
        byte b3 = (byte) ((value & 0xff));
        byte b2 = (byte) ((value >> 8 * 1) & 0xff);
        byte b1 = (byte) ((value >> 8 * 2) & 0xff);
        byte b0 = (byte) ((value >> 8 * 3));
        memory.put(b0);
        memory.put(b1);
        memory.put(b2);
        memory.put(b3);
    }

    @Override
    public void putUnsignedIntLE(long value) {
        byte b0 = (byte) ((value & 0xff));
        byte b1 = (byte) ((value >> 8 * 1) & 0xff);
        byte b2 = (byte) ((value >> 8 * 2) & 0xff);
        byte b3 = (byte) ((value >> 8 * 3));
        memory.put(b0);
        memory.put(b1);
        memory.put(b2);
        memory.put(b3);
    }

    @Override
    public void putUnsignedShort(int value) {
        byte b1 = (byte) (value & 0xff);
        byte b0 = (byte) (value >> 8 * 1);
        memory.put(b0);
        memory.put(b1);
    }

    @Override
    public void putUnsignedShortLE(int value) {
        byte b0 = (byte) (value & 0xff);
        byte b1 = (byte) (value >> 8 * 1);
        memory.put(b0);
        memory.put(b1);
    }

    @Override
    protected int read0(ByteBuf src, int read) {
        long dstAddr = UnsafeUtil.addressOffset(memory) + memory.position();
        if (src.hasArray()) {
            UnsafeUtil.copyFromArray(src.array(), src.absPos(), dstAddr, read);
        } else {
            UnsafeUtil.copyMemory(src.nioBuffer(), dstAddr, read);
        }
        src.skip(read);
        skip(read);
        return read;
    }

    @Override
    protected int read0(ByteBuffer src, int read) {
        long dstAddr = UnsafeUtil.addressOffset(memory) + memory.position();
        if (src.hasArray()) {
            UnsafeUtil.copyFromArray(src, dstAddr, read);
        } else {
            UnsafeUtil.copyMemory(src, dstAddr, read);
        }
        src.position(src.position() + read);
        skip(read);
        return read;
    }

    @Override
    public int remaining() {
        return memory.remaining();
    }

    @Override
    public ByteBuf resetL() {
        memory.limit(markLimit);
        return this;
    }
    
    @Override
    public ByteBuf resetP() {
        memory.reset();
        return this;
    }

    @Override
    public ByteBuf reverse() {
        return this;
    }

    @Override
    public ByteBuf skip(int length) {
        memory.position(memory.position() + length);
        return this;
    }

    private long toUnsignedInt(int value) {
        if (value < 0) {
            return value & 0xffffffffffffffffL;
        }
        return value;
    }

}
