/*
 * Copyright 2015 The FireNio Project
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
package com.firenio.buffer;

import java.nio.ByteBuffer;

import com.firenio.common.ByteUtil;
import com.firenio.common.Unsafe;

abstract class HeapByteBuf extends ByteBuf {

    protected int        limit;
    protected int        markLimit;
    protected int        markPos;
    protected byte[]     memory;
    protected ByteBuffer nioBuffer;
    protected int        pos;

    HeapByteBuf(byte[] memory) {
        this.memory = memory;
    }

    HeapByteBuf(ByteBuffer memory) {
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
        return pos;
    }

    @Override
    public ByteBuf absPos(int pos) {
        this.pos = pos;
        return this;
    }

    @Override
    public byte[] array() {
        return memory;
    }

    @Override
    public ByteBuf clear() {
        this.pos = offset();
        this.limit = ix(capacity());
        return this;
    }

    @Override
    public ByteBuf flip() {
        this.limit = pos;
        this.pos = offset();
        return this;
    }

    @Override
    protected int get0(ByteBuffer dst, int len) {
        if (dst.hasArray()) {
            copy(memory, absPos(), dst.array(), dst.position(), len);
        } else {
            copy(memory, absPos(), Unsafe.address(dst) + dst.position(), len);
        }
        dst.position(dst.position() + len);
        skip(len);
        return len;
    }

    @Override
    public byte getByte() {
        return memory[pos++];
    }

    @Override
    public byte getByte(int index) {
        return memory[ix(index)];
    }

    @Override
    public void getBytes(byte[] dst, int offset, int length) {
        System.arraycopy(memory, pos, dst, offset, length);
        this.pos += length;
    }

    @Override
    public int getInt() {
        int v = ByteUtil.getInt(memory, pos);
        this.pos += 4;
        return v;
    }

    @Override
    public int getInt(int index) {
        return ByteUtil.getInt(memory, ix(index));
    }

    @Override
    public int getIntLE() {
        int v = ByteUtil.getIntLE(memory, pos);
        this.pos += 4;
        return v;
    }

    @Override
    public int getIntLE(int offset) {
        return ByteUtil.getIntLE(memory, ix(offset));
    }

    @Override
    public long getLong() {
        long v = ByteUtil.getLong(memory, pos);
        this.pos += 8;
        return v;
    }

    @Override
    public long getLong(int index) {
        return ByteUtil.getLong(memory, ix(index));
    }

    @Override
    public long getLongLE() {
        long v = ByteUtil.getLongLE(memory, pos);
        this.pos += 8;
        return v;
    }

    @Override
    public long getLongLE(int index) {
        return ByteUtil.getLongLE(memory, ix(index));
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
        short v = ByteUtil.getShort(memory, pos);
        this.pos += 2;
        return v;
    }

    @Override
    public short getShort(int index) {
        return ByteUtil.getShort(memory, ix(index));
    }

    @Override
    public short getShortLE() {
        short v = ByteUtil.getShortLE(memory, pos);
        this.pos += 2;
        return v;
    }

    @Override
    public short getShortLE(int index) {
        return ByteUtil.getShortLE(memory, ix(index));
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
        long v = ByteUtil.getInt(memory, pos) & 0xffffffffL;
        this.pos += 4;
        return v;
    }

    @Override
    public long getUnsignedInt(int index) {
        return ByteUtil.getInt(memory, ix(index)) & 0xffffffffL;
    }

    @Override
    public long getUnsignedIntLE() {
        long v = ByteUtil.getIntLE(memory, pos) & 0xffffffffL;
        this.pos += 4;
        return v;
    }

    @Override
    public long getUnsignedIntLE(int index) {
        return ByteUtil.getIntLE(memory, ix(index)) & 0xffffffffL;
    }

    @Override
    public int getUnsignedShort() {
        int v = ByteUtil.getShort(memory, pos) & 0xffff;
        this.pos += 2;
        return v;
    }

    @Override
    public int getUnsignedShort(int index) {
        return ByteUtil.getShort(memory, ix(index)) & 0xffff;
    }

    @Override
    public int getUnsignedShortLE() {
        int v = ByteUtil.getShortLE(memory, pos) & 0xffff;
        this.pos += 2;
        return v;
    }

    @Override
    public int getUnsignedShortLE(int index) {
        return ByteUtil.getShortLE(memory, ix(index)) & 0xffff;
    }

    @Override
    public boolean hasArray() {
        return true;
    }

    @Override
    public boolean hasRemaining() {
        return pos < limit;
    }

    @Override
    public int indexOf(byte b, int abs_pos, int size) {
        int    p = abs_pos;
        int    l = p + size;
        byte[] m = memory;
        for (; p < l; p++) {
            if (m[p] == b) {
                return p;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(byte b, int abs_pos, int size) {
        int    p = abs_pos;
        int    l = p - size - 1;
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
        markPos = pos;
        return this;
    }

    @Override
    public ByteBuffer nioBuffer() {
        ByteBuffer buffer = getNioBuffer();
        return (ByteBuffer) buffer.limit(limit).position(pos);
    }

    @Override
    public int position() {
        return pos - offset();
    }

    @Override
    public ByteBuf position(int position) {
        this.pos = ix(position);
        return this;
    }

    @Override
    public void putByte(int index, byte b) {
        memory[ix(index)] = b;
    }

    @Override
    protected void putByte0(byte b) {
        memory[pos++] = b;
    }

    @Override
    protected int putBytes0(byte[] src, int offset, int length) {
        System.arraycopy(src, offset, memory, pos, length);
        this.pos += length;
        return length;
    }

    @Override
    protected int putBytes00(ByteBuf src, int len) {
        if (src.hasArray()) {
            copy(src.array(), src.absPos(), memory, absPos(), len);
        } else {
            copy(src.address() + src.absPos(), memory, absPos(), len);
        }
        src.skip(len);
        skip(len);
        return len;
    }

    @Override
    protected int putBytes00(ByteBuffer src, int len) {
        if (src.hasArray()) {
            copy(src.array(), src.position(), memory, absPos(), len);
        } else {
            copy(Unsafe.address(src) + src.position(), memory, absPos(), len);
        }
        src.position(src.position() + len);
        skip(len);
        return len;
    }

    @Override
    public void putInt(int index, int value) {
        ByteUtil.putInt(memory, value, ix(index));
    }

    @Override
    protected void putInt0(int value) {
        ByteUtil.putInt(memory, value, pos);
        pos += 4;
    }

    @Override
    public void putIntLE(int index, int value) {
        ByteUtil.putIntLE(memory, value, ix(index));
    }

    @Override
    protected void putIntLE0(int value) {
        ByteUtil.putIntLE(memory, value, pos);
        pos += 4;
    }

    @Override
    public void putLong(int index, long value) {
        ByteUtil.putLong(memory, value, ix(index));
    }

    @Override
    protected void putLong0(long value) {
        ByteUtil.putLong(memory, value, pos);
        pos += 8;
    }

    @Override
    public void putLongLE(int index, long value) {
        ByteUtil.putLongLE(memory, value, ix(index));
    }

    @Override
    protected void putLongLE0(long value) {
        ByteUtil.putLongLE(memory, value, pos);
        pos += 8;
    }

    @Override
    public void putShort(int index, int value) {
        ByteUtil.putShort(memory, (short) value, ix(index));
    }

    @Override
    protected void putShort0(int value) {
        ByteUtil.putShort(memory, (short) value, pos);
        pos += 2;
    }

    @Override
    public void putShortLE(int index, int value) {
        ByteUtil.putShortLE(memory, (short) value, ix(index));
    }

    @Override
    protected void putShortLE0(int value) {
        ByteUtil.putShortLE(memory, (short) value, pos);
        pos += 2;
    }

    @Override
    public int remaining() {
        return limit - pos;
    }

    @Override
    public ByteBuf resetL() {
        limit = markLimit;
        return this;
    }

    @Override
    public ByteBuf resetP() {
        pos = markPos;
        return this;
    }

    @Override
    public ByteBuf reverse() {
        pos = nioBuffer.position();
        return this;
    }

    @Override
    public ByteBuf skip(int length) {
        pos += length;
        return this;
    }

}
