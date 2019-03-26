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

import com.firenio.baseio.common.Unsafe;

abstract class DirectByteBuf extends ByteBuf {

    protected int        markLimit;
    protected ByteBuffer memory;

    DirectByteBuf(ByteBuffer memory) {
        this.memory = memory;
    }

    @Override
    public byte absByte(int pos) {
        return memory.get(pos);
    }

    @Override
    public int absLimit() {
        return memory.limit();
    }

    @Override
    public ByteBuf absLimit(int limit) {
        this.memory.limit(limit);
        return this;
    }

    @Override
    public int absPos() {
        return memory.position();
    }

    @Override
    public ByteBuf absPos(int pos) {
        this.memory.position(pos);
        return this;
    }

    @Override
    public byte[] array() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ByteBuf clear() {
        memory.position(offset()).limit(ix(capacity()));
        return this;
    }

    @Override
    public ByteBuf flip() {
        memory.limit(memory.position());
        memory.position(offset());
        return this;
    }

    @Override
    public void getBytes(byte[] dst, int offset, int length) {
        memory.get(dst, offset, length);
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
        return Integer.reverseBytes(memory.getInt());
    }

    @Override
    public int getIntLE(int index) {
        return Integer.reverseBytes(memory.getInt(ix(index)));
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
        return Long.reverseBytes(memory.getLong());
    }

    @Override
    public long getLongLE(int index) {
        return Long.reverseBytes(memory.getLong(ix(index)));
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
        return Short.reverseBytes(memory.getShort());
    }

    @Override
    public short getShortLE(int index) {
        return Short.reverseBytes(memory.getShort(ix(index)));
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
        return toUnsignedInt(Integer.reverseBytes(memory.getInt()));
    }

    @Override
    public long getUnsignedIntLE(int index) {
        return toUnsignedInt(Integer.reverseBytes(memory.getInt(ix(index))));
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
        return Short.reverseBytes(memory.getShort()) & 0xffff;
    }

    @Override
    public int getUnsignedShortLE(int index) {
        return Short.reverseBytes(memory.getShort(ix(index))) & 0xffff;
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
    public int indexOf(byte b, int abs_pos, int size) {
        ByteBuffer m = memory;
        if (Unsafe.ENABLE) {
            long addr = address();
            long p = addr + abs_pos;
            long l = p + size;
            for (; p < l; p++) {
                if (Unsafe.getByte(p) == b) {
                    return (int) (p - addr);
                }
            }
        } else {
            int p = abs_pos;
            int l = p + size;
            for (; p < l; p++) {
                if (m.get(p) == b) {
                    return p;
                }
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(byte b, int abs_pos, int size) {
        ByteBuffer m = memory;
        if (Unsafe.ENABLE) {
            long addr = address();
            long p = addr + abs_pos;
            long l = p - size - 1;
            for (; p > l; p--) {
                if (Unsafe.getByte(p) == b) {
                    return (int) (p - addr);
                }
            }
        } else {
            int p = abs_pos;
            int l = p - size - 1;
            for (; p > l; p--) {
                if (m.get(p) == b) {
                    return p;
                }
            }
        }
        return -1;
    }

    @Override
    public int limit() {
        return memory.limit() - offset();
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
        return memory.position() - offset();
    }

    @Override
    public ByteBuf position(int position) {
        memory.position(ix(position));
        return this;
    }

    @Override
    protected void putBytes0(byte[] src, int offset, int length) {
        memory.put(src, offset, length);
    }

    @Override
    protected int putBytes00(ByteBuf src, int len) {
        if (src.hasArray()) {
            copy(src.array(), src.position(), address() + absPos(), len);
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
        memory.put(ix(index), b);
    }

    @Override
    protected void putByte0(byte b) {
        memory.put(b);
    }

    @Override
    public void putInt(int index, int value) {
        memory.putInt(ix(index), value);
    }

    @Override
    public void putInt0(int value) {
        memory.putInt(value);
    }

    @Override
    public void putIntLE(int index, int value) {
        memory.putInt(ix(index), Integer.reverseBytes(value));
    }

    @Override
    protected void putIntLE0(int value) {
        memory.putInt(Integer.reverseBytes(value));
    }

    @Override
    public void putLong(int index, long value) {
        memory.putLong(ix(index), value);
    }

    @Override
    protected void putLong0(long value) {
        memory.putLong(value);
    }

    @Override
    public void putLongLE(int index, long value) {
        memory.putLong(ix(index), Long.reverseBytes(value));
    }

    @Override
    protected void putLongLE0(long value) {
        memory.putLong(Long.reverseBytes(value));
    }

    @Override
    public void putShort(int index, short value) {
        memory.putShort(ix(index), value);
    }

    @Override
    protected void putShort0(short value) {
        memory.putShort(value);
    }

    @Override
    public void putShortLE(int index, short value) {
        memory.putShort(ix(index), Short.reverseBytes(value));
    }

    @Override
    protected void putShortLE0(short value) {
        memory.putShort(Short.reverseBytes(value));
    }

    @Override
    public void putUnsignedInt(int index, long value) {
        memory.putInt(ix(index), (int) value);
    }

    @Override
    protected void putUnsignedInt0(long value) {
        memory.putInt((int) value);
    }

    @Override
    public void putUnsignedIntLE(int index, long value) {
        memory.putInt(ix(index), Integer.reverseBytes((int) value));
    }

    @Override
    protected void putUnsignedIntLE0(long value) {
        memory.putInt(Integer.reverseBytes((int) value));
    }

    @Override
    public void putUnsignedShort(int index, int value) {
        memory.putShort(ix(index), (short) value);
    }

    @Override
    protected void putUnsignedShort0(int value) {
        memory.putShort((short) value);
    }

    @Override
    public void putUnsignedShortLE(int index, int value) {
        memory.putShort(ix(index), Short.reverseBytes((short) value));
    }

    @Override
    protected void putUnsignedShortLE0(int value) {
        memory.putShort(Short.reverseBytes((short) value));
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

    private static long toUnsignedInt(int value) {
        if (value < 0) {
            return value & 0xffffffffffffffffL;
        }
        return value;
    }

}
