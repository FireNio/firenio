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

import com.firenio.common.ByteUtil;
import com.firenio.common.Unsafe;

import java.nio.ByteBuffer;

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
        throw unsupportedOperationException();
    }

    @Override
    public void compact() {
        if (!hasReadableBytes()) {
            clear();
            return;
        }
        int remain     = readableBytes();
        int src_offset = absReadIndex();
        int dst_offset = offset();
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
    public int indexOf(byte b, int absFrom, int absTo) {
        byte[] m = memory;
        if (BUF_FAST_INDEX_OF) {
            int size = absTo - absFrom;
            if (size < 16) {
                return unroll_index_of(m, absFrom, size, b);
            } else {
                return index_of_16(m, absFrom, size, b);
            }
        }
        return plain_index_of(m, absFrom, absTo, b);
    }

    private static int index_of_16(byte[] m, int from, int size, byte b) {
        int group = 16;
        int count = (size >>> 4) << 4;
        int s1_to = from + count;
        for (int i = from; i < s1_to; i += group) {
            if (m[i] == b) {
                return i;
            }
            if (m[i + 1] == b) {
                return i + 1;
            }
            if (m[i + 2] == b) {
                return i + 2;
            }
            if (m[i + 3] == b) {
                return i + 3;
            }
            if (m[i + 4] == b) {
                return i + 4;
            }
            if (m[i + 5] == b) {
                return i + 5;
            }
            if (m[i + 6] == b) {
                return i + 6;
            }
            if (m[i + 7] == b) {
                return i + 7;
            }
            if (m[i + 8] == b) {
                return i + 8;
            }
            if (m[i + 9] == b) {
                return i + 9;
            }
            if (m[i + 10] == b) {
                return i + 10;
            }
            if (m[i + 11] == b) {
                return i + 11;
            }
            if (m[i + 12] == b) {
                return i + 12;
            }
            if (m[i + 13] == b) {
                return i + 13;
            }
            if (m[i + 14] == b) {
                return i + 14;
            }
            if (m[i + 15] == b) {
                return i + 15;
            }
        }
        return unroll_index_of(m, s1_to, size & (group - 1), b);
    }

    private static int unroll_index_of(byte[] m, int from, int size, byte b) {
        switch (size) {
            case 1:
                if (m[from] == b) {
                    return from;
                }
                break;
            case 2:
                if (m[from] == b) {
                    return from;
                }
                if (m[from + 1] == b) {
                    return from + 1;
                }
                break;
            case 3:
                if (m[from] == b) {
                    return from;
                }
                if (m[from + 1] == b) {
                    return from + 1;
                }
                if (m[from + 2] == b) {
                    return from + 2;
                }
                break;
            case 4:
                if (m[from] == b) {
                    return from;
                }
                if (m[from + 1] == b) {
                    return from + 1;
                }
                if (m[from + 2] == b) {
                    return from + 2;
                }
                if (m[from + 3] == b) {
                    return from + 3;
                }
                break;
            case 5:
                if (m[from] == b) {
                    return from;
                }
                if (m[from + 1] == b) {
                    return from + 1;
                }
                if (m[from + 2] == b) {
                    return from + 2;
                }
                if (m[from + 3] == b) {
                    return from + 3;
                }
                if (m[from + 4] == b) {
                    return from + 4;
                }
                break;
            case 6:
                if (m[from] == b) {
                    return from;
                }
                if (m[from + 1] == b) {
                    return from + 1;
                }
                if (m[from + 2] == b) {
                    return from + 2;
                }
                if (m[from + 3] == b) {
                    return from + 3;
                }
                if (m[from + 4] == b) {
                    return from + 4;
                }
                if (m[from + 5] == b) {
                    return from + 5;
                }
                break;
            case 7:
                if (m[from] == b) {
                    return from;
                }
                if (m[from + 1] == b) {
                    return from + 1;
                }
                if (m[from + 2] == b) {
                    return from + 2;
                }
                if (m[from + 3] == b) {
                    return from + 3;
                }
                if (m[from + 4] == b) {
                    return from + 4;
                }
                if (m[from + 5] == b) {
                    return from + 5;
                }
                if (m[from + 6] == b) {
                    return from + 6;
                }
                break;
            case 8:
                if (m[from] == b) {
                    return from;
                }
                if (m[from + 1] == b) {
                    return from + 1;
                }
                if (m[from + 2] == b) {
                    return from + 2;
                }
                if (m[from + 3] == b) {
                    return from + 3;
                }
                if (m[from + 4] == b) {
                    return from + 4;
                }
                if (m[from + 5] == b) {
                    return from + 5;
                }
                if (m[from + 6] == b) {
                    return from + 6;
                }
                if (m[from + 7] == b) {
                    return from + 7;
                }
                break;
            case 9:
                if (m[from] == b) {
                    return from;
                }
                if (m[from + 1] == b) {
                    return from + 1;
                }
                if (m[from + 2] == b) {
                    return from + 2;
                }
                if (m[from + 3] == b) {
                    return from + 3;
                }
                if (m[from + 4] == b) {
                    return from + 4;
                }
                if (m[from + 5] == b) {
                    return from + 5;
                }
                if (m[from + 6] == b) {
                    return from + 6;
                }
                if (m[from + 7] == b) {
                    return from + 7;
                }
                if (m[from + 8] == b) {
                    return from + 8;
                }
                break;
            case 10:
                if (m[from] == b) {
                    return from;
                }
                if (m[from + 1] == b) {
                    return from + 1;
                }
                if (m[from + 2] == b) {
                    return from + 2;
                }
                if (m[from + 3] == b) {
                    return from + 3;
                }
                if (m[from + 4] == b) {
                    return from + 4;
                }
                if (m[from + 5] == b) {
                    return from + 5;
                }
                if (m[from + 6] == b) {
                    return from + 6;
                }
                if (m[from + 7] == b) {
                    return from + 7;
                }
                if (m[from + 8] == b) {
                    return from + 8;
                }
                if (m[from + 9] == b) {
                    return from + 9;
                }
                break;
            case 11:
                if (m[from] == b) {
                    return from;
                }
                if (m[from + 1] == b) {
                    return from + 1;
                }
                if (m[from + 2] == b) {
                    return from + 2;
                }
                if (m[from + 3] == b) {
                    return from + 3;
                }
                if (m[from + 4] == b) {
                    return from + 4;
                }
                if (m[from + 5] == b) {
                    return from + 5;
                }
                if (m[from + 6] == b) {
                    return from + 6;
                }
                if (m[from + 7] == b) {
                    return from + 7;
                }
                if (m[from + 8] == b) {
                    return from + 8;
                }
                if (m[from + 9] == b) {
                    return from + 9;
                }
                if (m[from + 10] == b) {
                    return from + 10;
                }
                break;
            case 12:
                if (m[from] == b) {
                    return from;
                }
                if (m[from + 1] == b) {
                    return from + 1;
                }
                if (m[from + 2] == b) {
                    return from + 2;
                }
                if (m[from + 3] == b) {
                    return from + 3;
                }
                if (m[from + 4] == b) {
                    return from + 4;
                }
                if (m[from + 5] == b) {
                    return from + 5;
                }
                if (m[from + 6] == b) {
                    return from + 6;
                }
                if (m[from + 7] == b) {
                    return from + 7;
                }
                if (m[from + 8] == b) {
                    return from + 8;
                }
                if (m[from + 9] == b) {
                    return from + 9;
                }
                if (m[from + 10] == b) {
                    return from + 10;
                }
                if (m[from + 11] == b) {
                    return from + 11;
                }
                break;
            case 13:
                if (m[from] == b) {
                    return from;
                }
                if (m[from + 1] == b) {
                    return from + 1;
                }
                if (m[from + 2] == b) {
                    return from + 2;
                }
                if (m[from + 3] == b) {
                    return from + 3;
                }
                if (m[from + 4] == b) {
                    return from + 4;
                }
                if (m[from + 5] == b) {
                    return from + 5;
                }
                if (m[from + 6] == b) {
                    return from + 6;
                }
                if (m[from + 7] == b) {
                    return from + 7;
                }
                if (m[from + 8] == b) {
                    return from + 8;
                }
                if (m[from + 9] == b) {
                    return from + 9;
                }
                if (m[from + 10] == b) {
                    return from + 10;
                }
                if (m[from + 11] == b) {
                    return from + 11;
                }
                if (m[from + 12] == b) {
                    return from + 12;
                }
                break;
            case 14:
                if (m[from] == b) {
                    return from;
                }
                if (m[from + 1] == b) {
                    return from + 1;
                }
                if (m[from + 2] == b) {
                    return from + 2;
                }
                if (m[from + 3] == b) {
                    return from + 3;
                }
                if (m[from + 4] == b) {
                    return from + 4;
                }
                if (m[from + 5] == b) {
                    return from + 5;
                }
                if (m[from + 6] == b) {
                    return from + 6;
                }
                if (m[from + 7] == b) {
                    return from + 7;
                }
                if (m[from + 8] == b) {
                    return from + 8;
                }
                if (m[from + 9] == b) {
                    return from + 9;
                }
                if (m[from + 10] == b) {
                    return from + 10;
                }
                if (m[from + 11] == b) {
                    return from + 11;
                }
                if (m[from + 12] == b) {
                    return from + 12;
                }
                if (m[from + 13] == b) {
                    return from + 13;
                }
                break;
            case 15:
                if (m[from] == b) {
                    return from;
                }
                if (m[from + 1] == b) {
                    return from + 1;
                }
                if (m[from + 2] == b) {
                    return from + 2;
                }
                if (m[from + 3] == b) {
                    return from + 3;
                }
                if (m[from + 4] == b) {
                    return from + 4;
                }
                if (m[from + 5] == b) {
                    return from + 5;
                }
                if (m[from + 6] == b) {
                    return from + 6;
                }
                if (m[from + 7] == b) {
                    return from + 7;
                }
                if (m[from + 8] == b) {
                    return from + 8;
                }
                if (m[from + 9] == b) {
                    return from + 9;
                }
                if (m[from + 10] == b) {
                    return from + 10;
                }
                if (m[from + 11] == b) {
                    return from + 11;
                }
                if (m[from + 12] == b) {
                    return from + 12;
                }
                if (m[from + 13] == b) {
                    return from + 13;
                }
                if (m[from + 14] == b) {
                    return from + 14;
                }
                break;

        }
        return -1;
    }

    private static int plain_index_of(byte[] m, int from, int to, byte b) {
        for (; from < to; from++) {
            if (m[from] == b) {
                return from;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(byte b, int absFrom, int absTo) {
        int    p = absFrom;
        byte[] m = memory;
        for (; p > absTo; p--) {
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
    public int setBytes0(int index, long address, int len) {
        copy(address, memory, ix(index), len);
        return len;
    }

    @Override
    public int writeBytes0(long address, int len) {
        copy(address, memory, absWriteIndex(), len);
        skipWrite(len);
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
