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

    protected byte[]     memory;
    protected ByteBuffer nioBuffer;

    HeapByteBuf(byte[] memory) {
        this.memory = memory;
    }

    HeapByteBuf(ByteBuffer memory) {
        this.nioBuffer = memory;
        this.memory = memory.array();
    }

    @Override
    public byte[] array() {
        return memory;
    }

    @Override
    public long address() {
        return -1;
    }

    @Override
    public void compact() {
        if (!hasReadableBytes()) {
            clear();
            return;
        }
        int remain          = readableBytes();
        int abs_read_index  = absReadIndex();
        int abs_write_index = absWriteIndex();
        int src_offset      = abs_read_index;
        int dst_offset      = offset();
        System.arraycopy(memory, src_offset, memory, dst_offset, remain);
        readIndex(0);
        writeIndex(remain);
    }

    @Override
    public ByteBuf duplicate() {
        if (isReleased()) {
            throw new IllegalStateException("released");
        }
        this.retain();
        return new DuplicatedHeapByteBuf(this, 1);
    }

    @Override
    public byte getByteAbs(int pos) {
        return memory[pos];
    }


    @Override
    public void getBytes(int index, byte[] dst, int offset, int length) {
        System.arraycopy(memory, ix(index), dst, offset, length);
    }

    @Override
    protected int getBytes0(int index, ByteBuf dst, int len) {
        if (dst.hasArray()) {
            copy(memory, ix(index), dst.array(), dst.writeIndex(), len);
        } else {
            copy(memory, ix(index), dst.address() + dst.absWriteIndex(), len);
        }
        dst.skipWrite(len);
        return len;
    }

    @Override
    protected int getBytes0(int index, ByteBuffer dst, int len) {
        if (dst.hasArray()) {
            copy(memory, ix(index), dst.array(), dst.position(), len);
        } else {
            copy(memory, ix(index), Unsafe.address(dst) + dst.position(), len);
        }
        dst.position(dst.position() + len);
        return len;
    }

    @Override
    public void readBytes(byte[] dst, int offset, int length) {
        System.arraycopy(memory, absReadIndex(), dst, offset, length);
        skipRead(length);
    }

    @Override
    protected int readBytes0(ByteBuf dst, int len) {
        if (dst.hasArray()) {
            copy(memory, absReadIndex(), dst.array(), dst.writeIndex(), len);
        } else {
            copy(memory, absReadIndex(), dst.address() + dst.absWriteIndex(), len);
        }
        dst.skipWrite(len);
        skipRead(len);
        return len;
    }

    @Override
    protected int readBytes0(ByteBuffer dst, int len) {
        if (dst.hasArray()) {
            copy(memory, absReadIndex(), dst.array(), dst.position(), len);
        } else {
            copy(memory, absReadIndex(), Unsafe.address(dst) + dst.position(), len);
        }
        dst.position(dst.position() + len);
        skipRead(len);
        return len;
    }

    @Override
    public byte readByte() {
        return memory[abs_read_index++];
    }

    @Override
    public byte getByte(int index) {
        return memory[ix(index)];
    }

    @Override
    public int readInt() {
        int v = ByteUtil.getInt(memory, absReadIndex());
        skipRead(4);
        return v;
    }

    @Override
    public int getInt(int index) {
        return ByteUtil.getInt(memory, ix(index));
    }

    @Override
    public int readIntLE() {
        int v = ByteUtil.getIntLE(memory, absReadIndex());
        skipRead(4);
        return v;
    }

    @Override
    public int getIntLE(int offset) {
        return ByteUtil.getIntLE(memory, ix(offset));
    }

    @Override
    public long readLong() {
        long v = ByteUtil.getLong(memory, absReadIndex());
        skipRead(8);
        return v;
    }

    @Override
    public long getLong(int index) {
        return ByteUtil.getLong(memory, ix(index));
    }

    @Override
    public long readLongLE() {
        long v = ByteUtil.getLongLE(memory, absReadIndex());
        skipRead(8);
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
    public short readShort() {
        short v = ByteUtil.getShort(memory, absReadIndex());
        skipRead(2);
        return v;
    }

    @Override
    public short getShort(int index) {
        return ByteUtil.getShort(memory, ix(index));
    }

    @Override
    public short readShortLE() {
        short v = ByteUtil.getShortLE(memory, absReadIndex());
        skipRead(2);
        return v;
    }

    @Override
    public short getShortLE(int index) {
        return ByteUtil.getShortLE(memory, ix(index));
    }

    @Override
    public short readUnsignedByte() {
        return (short) (readByte() & 0xff);
    }

    @Override
    public short getUnsignedByte(int index) {
        return (short) (getByte(index) & 0xff);
    }

    @Override
    public long readUnsignedInt() {
        long v = toUnsignedInt(ByteUtil.getInt(memory, absReadIndex()));
        skipRead(4);
        return v;
    }

    @Override
    public long getUnsignedInt(int index) {
        return toUnsignedInt(ByteUtil.getInt(memory, ix(index)));
    }

    @Override
    public long readUnsignedIntLE() {
        long v = toUnsignedInt(ByteUtil.getIntLE(memory, absReadIndex()));
        skipRead(4);
        return v;
    }

    @Override
    public long getUnsignedIntLE(int index) {
        return toUnsignedInt(ByteUtil.getIntLE(memory, ix(index)));
    }

    @Override
    public int readUnsignedShort() {
        int v = ByteUtil.getShort(memory, absReadIndex()) & 0xffff;
        skipRead(2);
        return v;
    }

    @Override
    public int getUnsignedShort(int index) {
        return ByteUtil.getShort(memory, ix(index)) & 0xffff;
    }

    @Override
    public int readUnsignedShortLE() {
        int v = ByteUtil.getShortLE(memory, absReadIndex()) & 0xffff;
        skipRead(2);
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
    public void setByte(int index, byte b) {
        memory[ix(index)] = b;
    }

    @Override
    protected void writeByte0(byte b) {
        memory[abs_write_index++] = b;
    }

    @Override
    protected int setBytes0(int index, byte[] src, int offset, int length) {
        System.arraycopy(src, offset, memory, ix(index), length);
        return length;
    }

    @Override
    protected int setBytes0(int index, ByteBuf src, int len) {
        if (src.hasArray()) {
            copy(src.array(), src.absReadIndex(), memory, ix(index), len);
        } else {
            copy(src.address() + src.absReadIndex(), memory, ix(index), len);
        }
        src.skipRead(len);
        return len;
    }

    @Override
    protected int setBytes0(int index, ByteBuffer src, int len) {
        if (src.hasArray()) {
            copy(src.array(), src.position(), memory, ix(index), len);
        } else {
            copy(Unsafe.address(src) + src.position(), memory, ix(index), len);
        }
        src.position(src.position() + len);
        return len;
    }

    @Override
    protected int writeBytes0(byte[] src, int offset, int length) {
        System.arraycopy(src, offset, memory, absWriteIndex(), length);
        skipWrite(length);
        return length;
    }

    @Override
    protected int writeBytes0(ByteBuf src, int len) {
        if (src.hasArray()) {
            copy(src.array(), src.absReadIndex(), memory, absWriteIndex(), len);
        } else {
            copy(src.address() + src.absReadIndex(), memory, absWriteIndex(), len);
        }
        src.skipRead(len);
        skipWrite(len);
        return len;
    }

    @Override
    protected int writeBytes0(ByteBuffer src, int len) {
        if (src.hasArray()) {
            copy(src.array(), src.position(), memory, absWriteIndex(), len);
        } else {
            copy(Unsafe.address(src) + src.position(), memory, absWriteIndex(), len);
        }
        src.position(src.position() + len);
        skipWrite(len);
        return len;
    }

    @Override
    public void setInt(int index, int value) {
        ByteUtil.putInt(memory, value, ix(index));
    }

    @Override
    protected void writeInt0(int value) {
        ByteUtil.putInt(memory, value, absWriteIndex());
        skipWrite(4);
    }

    @Override
    public void setIntLE(int index, int value) {
        ByteUtil.putIntLE(memory, value, ix(index));
    }

    @Override
    protected void writeIntLE0(int value) {
        ByteUtil.putIntLE(memory, value, absWriteIndex());
        skipWrite(4);
    }

    @Override
    public void setLong(int index, long value) {
        ByteUtil.putLong(memory, value, ix(index));
    }

    @Override
    protected void writeLong0(long value) {
        ByteUtil.putLong(memory, value, absWriteIndex());
        skipWrite(8);
    }

    @Override
    public void setLongLE(int index, long value) {
        ByteUtil.putLongLE(memory, value, ix(index));
    }

    @Override
    protected void writeLongLE0(long value) {
        ByteUtil.putLongLE(memory, value, absWriteIndex());
        skipWrite(8);
    }

    @Override
    public void setShort(int index, int value) {
        ByteUtil.putShort(memory, (short) value, ix(index));
    }

    @Override
    protected void writeShort0(int value) {
        ByteUtil.putShort(memory, (short) value, absWriteIndex());
        skipWrite(2);
    }

    @Override
    public void setShortLE(int index, int value) {
        ByteUtil.putShortLE(memory, (short) value, ix(index));
    }

    @Override
    protected void writeShortLE0(int value) {
        ByteUtil.putShortLE(memory, (short) value, absWriteIndex());
        skipWrite(2);
    }

    public ByteBuf reverseRead() {
        abs_read_index = getNioBuffer().position();
        return this;
    }

    public ByteBuf reverseWrite() {
        abs_write_index = getNioBuffer().position();
        return this;
    }

}
