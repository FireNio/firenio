/*
 * Copyright 2015 The Baseio Project
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
package com.firenio.baseio.buffer;

import java.nio.ByteBuffer;

import com.firenio.baseio.common.ByteUtil;

abstract class HeapByteBuf extends ByteBuf {

    protected ByteBufAllocator allocator;
    protected int              limit;
    protected int              markLimit;
    protected int              markPos;
    protected byte[]           memory;
    protected ByteBuffer       nioBuffer;
    protected int              position;

    HeapByteBuf(ByteBufAllocator allocator, byte[] memory) {
        this.allocator = allocator;
        this.memory = memory;
    }

    HeapByteBuf(ByteBufAllocator allocator, ByteBuffer memory) {
        this.allocator = allocator;
        this.nioBuffer = memory;
        this.memory = memory.array();
    }

    @Override
    public byte absByte(int pos) {
        return memory[pos];
    }

    @Override
    public int absLimit() {
        return limit;
    }

    @Override
    public ByteBuf absLimit(int limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public int absPos() {
        return position;
    }

    @Override
    public ByteBuf absPos(int pos) {
        this.position = pos;
        return this;
    }

    @Override
    public byte[] array() {
        return memory;
    }

    @Override
    public ByteBuf clear() {
        this.position = offset();
        this.limit = ix(capacity());
        return this;
    }

    @Override
    public ByteBuf flip() {
        this.limit = position;
        this.position = offset();
        return this;
    }

    @Override
    public void get(byte[] dst, int offset, int length) {
        System.arraycopy(memory, position, dst, offset, length);
        this.position += length;
    }

    @Override
    protected int get0(ByteBuffer dst, int len) {
        if (dst.hasArray()) {
            copy(memory, absPos(), dst.array(), dst.position(), len);
        } else {
            copy(memory, absPos(), dst, len);
        }
        dst.position(dst.position() + len);
        skip(len);
        return len;
    }

    @Override
    public byte getByte() {
        return memory[position++];
    }

    @Override
    public byte getByte(int index) {
        return memory[ix(index)];
    }

    @Override
    public int getInt() {
        int v = ByteUtil.byte2Int(memory, position);
        this.position += 4;
        return v;
    }

    @Override
    public int getInt(int index) {
        return ByteUtil.byte2Int(memory, ix(index));
    }

    @Override
    public int getIntLE() {
        int v = ByteUtil.byte2IntLE(memory, position);
        this.position += 4;
        return v;
    }

    @Override
    public int getIntLE(int offset) {
        return ByteUtil.byte2IntLE(memory, ix(offset));
    }

    @Override
    public long getLong() {
        long v = ByteUtil.byte2Long(memory, position);
        this.position += 8;
        return v;
    }

    @Override
    public long getLong(int index) {
        return ByteUtil.byte2Long(memory, ix(index));
    }

    @Override
    public long getLongLE() {
        long v = ByteUtil.byte2LongLE(memory, position);
        this.position += 8;
        return v;
    }

    @Override
    public long getLongLE(int index) {
        return ByteUtil.byte2LongLE(memory, ix(index));
    }

    @Override
    public ByteBuffer getNioBuffer() {
        if (nioBuffer == null) {
            nioBuffer = ByteBuffer.wrap(memory);
        }
        return nioBuffer;
    }

    @Override
    public short getShort() {
        short v = ByteUtil.byte2Short(memory, position);
        this.position += 2;
        return v;
    }

    @Override
    public short getShort(int index) {
        return ByteUtil.byte2Short(memory, ix(index));
    }

    @Override
    public short getShortLE() {
        short v = ByteUtil.byte2ShortLE(memory, position);
        this.position += 2;
        return v;
    }

    @Override
    public short getShortLE(int index) {
        return ByteUtil.byte2ShortLE(memory, ix(index));
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
        long v = ByteUtil.byte2UnsignedInt(memory, position);
        this.position += 4;
        return v;
    }

    @Override
    public long getUnsignedInt(int index) {
        return ByteUtil.byte2UnsignedInt(memory, ix(index));
    }

    @Override
    public long getUnsignedIntLE() {
        long v = ByteUtil.byte2UnsignedIntLE(memory, position);
        this.position += 4;
        return v;
    }

    @Override
    public long getUnsignedIntLE(int index) {
        return ByteUtil.byte2UnsignedIntLE(memory, ix(index));
    }

    @Override
    public int getUnsignedShort() {
        int v = ByteUtil.byte2UnsignedShort(memory, position);
        this.position += 2;
        return v;
    }

    @Override
    public int getUnsignedShort(int index) {
        return ByteUtil.byte2UnsignedShort(memory, ix(index));
    }

    @Override
    public int getUnsignedShortLE() {
        int v = ByteUtil.byte2UnsignedShortLE(memory, position);
        this.position += 2;
        return v;
    }

    @Override
    public int getUnsignedShortLE(int index) {
        return ByteUtil.byte2UnsignedShortLE(memory, ix(index));
    }

    @Override
    public boolean hasArray() {
        return true;
    }

    @Override
    public boolean hasRemaining() {
        return position < limit;
    }

    @Override
    public int indexOf(byte b, int absPos, int size) {
        int p = absPos;
        int l = p + size;
        byte[] m = memory;
        for (; p < l; p++) {
            if (m[p] == b) {
                return p;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(byte b, int absPos, int size) {
        int p = absPos;
        int l = p - size - 1;
        byte[] m = memory;
        for (; p > l; p--) {
            if (m[p] == b) {
                return p;
            }
        }
        return -1;
    }

    @Override
    public int limit() {
        return limit - offset();
    }

    @Override
    public ByteBuf limit(int limit) {
        this.limit = ix(limit);
        return this;
    }

    @Override
    public ByteBuf markL() {
        markLimit = limit;
        return this;
    }

    @Override
    public ByteBuf markP() {
        markPos = position;
        return this;
    }

    @Override
    public ByteBuffer nioBuffer() {
        ByteBuffer buffer = getNioBuffer();
        return (ByteBuffer) buffer.limit(limit).position(position);
    }

    @Override
    public int position() {
        return position - offset();
    }

    @Override
    public ByteBuf position(int position) {
        this.position = ix(position);
        return this;
    }

    @Override
    protected void put0(byte[] src, int offset, int length) {
        System.arraycopy(src, offset, memory, position, length);
        this.position += length;
    }

    @Override
    protected int put00(ByteBuf src, int len) {
        if (src.hasArray()) {
            copy(src.array(), src.absPos(), memory, absPos(), len);
        } else {
            copy(src.nioBuffer(), memory, absPos(), len);
        }
        src.skip(len);
        skip(len);
        return len;
    }

    @Override
    protected int put00(ByteBuffer src, int len) {
        if (src.hasArray()) {
            copy(src.array(), src.position(), memory, absPos(), len);
        } else {
            copy(src, memory, absPos(), len);
        }
        src.position(src.position() + len);
        skip(len);
        return len;
    }

    @Override
    public void putByte(int index, byte b) {
        memory[ix(index)] = b;
    }

    @Override
    protected void putByte0(byte b) {
        memory[position++] = b;
    }

    @Override
    public void putInt(int index, int value) {
        ByteUtil.int2Byte(memory, value, ix(index));
    }

    @Override
    protected void putInt0(int value) {
        ByteUtil.int2Byte(memory, value, position);
        position += 4;
    }

    @Override
    public void putIntLE(int index, int value) {
        ByteUtil.int2ByteLE(memory, value, ix(index));
    }

    @Override
    protected void putIntLE0(int value) {
        ByteUtil.int2ByteLE(memory, value, position);
        position += 4;
    }

    @Override
    public void putLong(int index, long value) {
        ByteUtil.long2Byte(memory, value, ix(index));
    }

    @Override
    protected void putLong0(long value) {
        ByteUtil.long2Byte(memory, value, position);
        position += 8;
    }

    @Override
    public void putLongLE(int index, long value) {
        ByteUtil.long2ByteLE(memory, value, ix(index));
    }

    @Override
    protected void putLongLE0(long value) {
        ByteUtil.long2ByteLE(memory, value, position);
        position += 8;
    }

    @Override
    public void putShort(int index, short value) {
        ByteUtil.short2Byte(memory, value, ix(index));
    }

    @Override
    protected void putShort0(short value) {
        ByteUtil.short2Byte(memory, value, position);
        position += 2;
    }

    @Override
    public void putShortLE(int index, short value) {
        ByteUtil.short2ByteLE(memory, value, ix(index));
    }

    @Override
    protected void putShortLE0(short value) {
        ByteUtil.short2ByteLE(memory, value, position);
        position += 2;
    }

    @Override
    public void putUnsignedInt(int index, long value) {
        ByteUtil.int2Byte(memory, (int) value, ix(index));
    }

    @Override
    protected void putUnsignedInt0(long value) {
        ByteUtil.unsignedInt2Byte(memory, value, position);
        position += 4;
    }

    @Override
    public void putUnsignedIntLE(int index, long value) {
        ByteUtil.int2ByteLE(memory, (int) value, ix(index));
    }

    @Override
    protected void putUnsignedIntLE0(long value) {
        ByteUtil.unsignedInt2ByteLE(memory, value, position);
        position += 4;
    }

    @Override
    public void putUnsignedShort(int index, int value) {
        ByteUtil.short2Byte(memory, (short) value, ix(index));
    }

    @Override
    protected void putUnsignedShort0(int value) {
        ByteUtil.unsignedShort2Byte(memory, value, position);
        position += 2;
    }

    @Override
    public void putUnsignedShortLE(int index, int value) {
        ByteUtil.short2ByteLE(memory, (short) value, ix(index));
    }

    @Override
    protected void putUnsignedShortLE0(int value) {
        ByteUtil.unsignedShort2ByteLE(memory, value, position);
        position += 2;
    }

    @Override
    public int remaining() {
        return limit - position;
    }

    @Override
    public ByteBuf resetL() {
        limit = markLimit;
        return this;
    }

    @Override
    public ByteBuf resetP() {
        position = markPos;
        return this;
    }

    @Override
    public ByteBuf reverse() {
        position = nioBuffer.position();
        return this;
    }

    @Override
    public ByteBuf skip(int length) {
        position += length;
        return this;
    }

}
