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
import com.firenio.baseio.common.Unsafe;

abstract class UnsafeByteBuf extends ByteBuf {

    protected int  capacity;
    protected int  limit;
    protected int  markLimit;
    protected int  markPos;
    protected long memory;
    protected int  pos;

    UnsafeByteBuf(long memory) {
        this.memory = memory;
    }

    @Override
    public int capacity() {
        return capacity;
    }

    @Override
    public byte absByte(int pos) {
        return Unsafe.getByte(memory + pos);
    }

    @Override
    public int absLimit() {
        return limit;
    }

    @Override
    public long address() {
        return memory;
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
        return null;
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
    public void getBytes(byte[] dst, int offset, int length) {
        Unsafe.copyToArray(memory + absPos(), dst, offset, length);
        this.skip(length);
    }

    @Override
    protected int get0(ByteBuffer dst, int len) {
        if (dst.hasArray()) {
            copy(address() + absPos(), dst.array(), dst.position(), len);
        } else {
            copy(address() + absPos(), Unsafe.address(dst) + dst.position(), len);
        }
        dst.position(dst.position() + len);
        skip(len);
        return len;
    }

    @Override
    public byte getByte() {
        return Unsafe.getByte(address() + (pos++));
    }

    @Override
    public byte getByte(int index) {
        return Unsafe.getByte(address() + ix(index));
    }

    @Override
    public int getInt() {
        int v = ByteUtil.getInt(address() + absPos());
        this.skip(4);
        return v;
    }

    @Override
    public int getInt(int index) {
        return ByteUtil.getInt(address() + ix(index));
    }

    @Override
    public int getIntLE() {
        int v = ByteUtil.getIntLE(address() + absPos());
        this.skip(4);
        return v;
    }

    @Override
    public int getIntLE(int index) {
        return ByteUtil.getIntLE(address() + ix(index));
    }

    @Override
    public long getLong() {
        long v = ByteUtil.getLong(address() + absPos());
        this.skip(8);
        return v;
    }

    @Override
    public long getLong(int index) {
        return ByteUtil.getLong(address() + ix(index));
    }

    @Override
    public long getLongLE() {
        long v = ByteUtil.getLongLE(address() + absPos());
        this.skip(8);
        return v;
    }

    @Override
    public long getLongLE(int index) {
        return ByteUtil.getLongLE(address() + ix(index));
    }

    @Override
    public ByteBuffer getNioBuffer() {
        return null;
    }

    @Override
    public short getShort() {
        short v = ByteUtil.getShort(address() + absPos());
        this.skip(2);
        return v;
    }

    @Override
    public short getShort(int index) {
        return ByteUtil.getShort(address() + ix(index));
    }

    @Override
    public short getShortLE() {
        short v = ByteUtil.getShortLE(address() + absPos());
        this.skip(2);
        return v;
    }

    @Override
    public short getShortLE(int index) {
        return ByteUtil.getShortLE(address() + ix(index));
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
        return getInt() & 0xffffffffL;
    }

    @Override
    public long getUnsignedInt(int index) {
        return getInt(index) & 0xffffffffL;
    }

    @Override
    public long getUnsignedIntLE() {
        long v = ByteUtil.getIntLE(address() + absPos()) & 0xffffffffL;
        this.skip(4);
        return v;
    }

    @Override
    public long getUnsignedIntLE(int index) {
        return ByteUtil.getIntLE(address() + ix(index)) & 0xffffffffL;
    }

    @Override
    public int getUnsignedShort() {
        int v = ByteUtil.getShort(address() + absPos()) & 0xffff;
        this.skip(2);
        return v;
    }

    @Override
    public int getUnsignedShort(int index) {
        return ByteUtil.getShort(address() + ix(index)) & 0xffff;
    }

    @Override
    public int getUnsignedShortLE() {
        int v = ByteUtil.getShortLE(address() + absPos()) & 0xffff;
        this.skip(2);
        return v;
    }

    @Override
    public int getUnsignedShortLE(int index) {
        return ByteUtil.getShortLE(address() + ix(index)) & 0xffff;
    }

    @Override
    public boolean hasArray() {
        return false;
    }

    @Override
    public boolean hasRemaining() {
        return pos < limit;
    }

    @Override
    public int indexOf(byte b, int abs_pos, int size) {
        long addr = address();
        long p = addr + abs_pos;
        long l = p + size;
        for (; p < l; p++) {
            if (Unsafe.getByte(p) == b) {
                return (int) (p - addr);
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(byte b, int abs_pos, int size) {
        long addr = address();
        long p = addr + abs_pos;
        long l = p - size;
        for (; p > l; p--) {
            if (Unsafe.getByte(p) == b) {
                return (int) (p - addr);
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
    protected void putBytes0(byte[] src, int offset, int length) {
        Unsafe.copyFromArray(src, offset, address() + absPos(), length);
        this.pos += length;
    }

    @Override
    protected int putBytes00(ByteBuf src, int len) {
        if (src.hasArray()) {
            copy(src.array(), src.absPos(), address() + absPos(), len);
        } else {
            copy(src.address() + src.absPos(), address() + absPos(), len);
        }
        src.skip(len);
        skip(len);
        return len;
    }

    @Override
    protected int putBytes00(ByteBuffer src, int len) {
        if (src.hasArray()) {
            copy(src.array(), src.position(), address() + absPos(), len);
        } else {
            copy(Unsafe.address(src) + src.position(), address() + absPos(), len);
        }
        src.position(src.position() + len);
        skip(len);
        return len;
    }

    @Override
    public void putByte(int index, byte b) {
        Unsafe.putByte(address() + ix(index), b);
    }

    @Override
    protected void putByte0(byte b) {
        Unsafe.putByte(address() + (pos++), b);
    }

    @Override
    public void putInt(int index, int value) {
        ByteUtil.putInt(address() + ix(index), value);
    }

    @Override
    protected void putInt0(int value) {
        ByteUtil.putInt(address() + absPos(), value);
        skip(4);
    }

    @Override
    public void putIntLE(int index, int value) {
        ByteUtil.putIntLE(address() + ix(index), value);
    }

    @Override
    protected void putIntLE0(int value) {
        ByteUtil.putIntLE(address() + absPos(), value);
        skip(4);
    }

    @Override
    public void putLong(int index, long value) {
        ByteUtil.putLong(address() + ix(index), value);
    }

    @Override
    protected void putLong0(long value) {
        ByteUtil.putLong(address() + absPos(), value);
        skip(8);
    }

    @Override
    public void putLongLE(int index, long value) {
        ByteUtil.putLongLE(address() + ix(index), value);
    }

    @Override
    protected void putLongLE0(long value) {
        ByteUtil.putLongLE(address() + absPos(), value);
        skip(8);
    }

    @Override
    public void putShort(int index, short value) {
        ByteUtil.putShort(address() + ix(index), value);
    }

    @Override
    protected void putShort0(short value) {
        ByteUtil.putShort(address() + absPos(), value);
        skip(2);
    }

    @Override
    public void putShortLE(int index, short value) {
        ByteUtil.putShortLE(address() + ix(index), value);
    }

    @Override
    protected void putShortLE0(short value) {
        ByteUtil.putShortLE(address() + absPos(), value);
        skip(2);
    }

    @Override
    public void putUnsignedInt(int index, long value) {
        ByteUtil.putInt(address() + ix(index), (int) value);
    }

    @Override
    protected void putUnsignedInt0(long value) {
        ByteUtil.putInt(address() + absPos(), (int) value);
        skip(4);
    }

    @Override
    public void putUnsignedIntLE(int index, long value) {
        ByteUtil.putIntLE(address() + ix(index), (int) value);
    }

    @Override
    protected void putUnsignedIntLE0(long value) {
        ByteUtil.putIntLE(address() + absPos(), (int) value);
        pos += 4;
    }

    @Override
    public void putUnsignedShort(int index, int value) {
        ByteUtil.putShort(address() + ix(index), (short) value);
    }

    @Override
    protected void putUnsignedShort0(int value) {
        ByteUtil.putShort(address() + absPos(), (short) value);
        skip(2);
    }

    @Override
    public void putUnsignedShortLE(int index, int value) {
        ByteUtil.putShortLE(address() + ix(index), (short) value);
    }

    @Override
    protected void putUnsignedShortLE0(int value) {
        ByteUtil.putShortLE(address() + absPos(), (short) value);
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
        return this;
    }

    @Override
    public ByteBuf skip(int length) {
        pos += length;
        return this;
    }

}
