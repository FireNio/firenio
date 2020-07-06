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

abstract class UnsafeByteBuf extends ByteBuf {

    protected long memory;

    UnsafeByteBuf(long memory) {
        this.memory = memory;
    }

    @Override
    public long address() {
        return memory;
    }

    @Override
    public byte[] array() {
        throw unsupportedOperationException();
    }

    @Override
    public void compact() {
        if (!hasReadableBytes()) {
            clear();
            return;
        }
        long address        = address();
        int  remain         = readableBytes();
        int  abs_read_index = absReadIndex();
        long src_addr       = address + abs_read_index;
        long dst_addr       = address + offset();
        Unsafe.copyMemory(src_addr, dst_addr, remain);
        readIndex(0);
        writeIndex(remain);
    }

    @Override
    public ByteBuf duplicate() {
        if (isReleased()) {
            throw new IllegalStateException("released");
        }
        this.retain();
        return new DuplicatedUnsafeByteBuf(this, 1);
    }

    @Override
    public byte getByteAbs(int pos) {
        return Unsafe.getByte(memory + pos);
    }

    @Override
    public void getBytes(int index, byte[] dst, int offset, int length) {
        Unsafe.copyToArray(memory + ix(index), dst, offset, length);
    }

    @Override
    protected int getBytes0(int index, ByteBuf dst, int len) {
        if (dst.hasArray()) {
            copy(address() + ix(index), dst.array(), dst.writeIndex(), len);
        } else {
            copy(address() + ix(index), dst.address() + dst.absWriteIndex(), len);
        }
        dst.skipWrite(len);
        return len;
    }

    @Override
    protected int getBytes0(int index, ByteBuffer dst, int len) {
        if (dst.hasArray()) {
            copy(address() + ix(index), dst.array(), dst.position(), len);
        } else {
            copy(address() + ix(index), Unsafe.address(dst) + dst.position(), len);
        }
        dst.position(dst.position() + len);
        return len;
    }

    @Override
    public void readBytes(byte[] dst, int offset, int length) {
        Unsafe.copyToArray(memory + absReadIndex(), dst, offset, length);
        this.skipRead(length);
    }

    @Override
    protected int readBytes0(ByteBuf dst, int len) {
        if (dst.hasArray()) {
            copy(address() + absReadIndex(), dst.array(), dst.writeIndex(), len);
        } else {
            copy(address() + absReadIndex(), dst.address() + dst.absWriteIndex(), len);
        }
        dst.skipWrite(len);
        skipRead(len);
        return len;
    }

    @Override
    protected int readBytes0(ByteBuffer dst, int len) {
        if (dst.hasArray()) {
            copy(address() + absReadIndex(), dst.array(), dst.position(), len);
        } else {
            copy(address() + absReadIndex(), Unsafe.address(dst) + dst.position(), len);
        }
        dst.position(dst.position() + len);
        skipRead(len);
        return len;
    }

    protected void setMemory(long memory) {
        this.memory = memory;
    }

    @Override
    public byte readByte() {
        return Unsafe.getByte(address() + abs_read_index++);
    }

    @Override
    public byte getByte(int index) {
        return Unsafe.getByte(address() + ix(index));
    }

    @Override
    public int readInt() {
        int v = ByteUtil.getInt(address() + absReadIndex());
        this.skipRead(4);
        return v;
    }

    @Override
    public int getInt(int index) {
        return ByteUtil.getInt(address() + ix(index));
    }

    @Override
    public int readIntLE() {
        int v = ByteUtil.getIntLE(address() + absReadIndex());
        this.skipRead(4);
        return v;
    }

    @Override
    public int getIntLE(int index) {
        return ByteUtil.getIntLE(address() + ix(index));
    }

    @Override
    public long readLong() {
        long v = ByteUtil.getLong(address() + absReadIndex());
        this.skipRead(8);
        return v;
    }

    @Override
    public long getLong(int index) {
        return ByteUtil.getLong(address() + ix(index));
    }

    @Override
    public long readLongLE() {
        long v = ByteUtil.getLongLE(address() + absReadIndex());
        this.skipRead(8);
        return v;
    }

    @Override
    public long getLongLE(int index) {
        return ByteUtil.getLongLE(address() + ix(index));
    }

    @Override
    public ByteBuffer getNioBuffer() {
        throw unsupportedOperationException();
    }

    @Override
    public short readShort() {
        short v = ByteUtil.getShort(address() + absReadIndex());
        this.skipRead(2);
        return v;
    }

    @Override
    public short getShort(int index) {
        return ByteUtil.getShort(address() + ix(index));
    }

    @Override
    public short readShortLE() {
        short v = ByteUtil.getShortLE(address() + absReadIndex());
        this.skipRead(2);
        return v;
    }

    @Override
    public short getShortLE(int index) {
        return ByteUtil.getShortLE(address() + ix(index));
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
        return toUnsignedInt(readInt());
    }

    @Override
    public long getUnsignedInt(int index) {
        return toUnsignedInt(getInt(index));
    }

    @Override
    public long readUnsignedIntLE() {
        long v = toUnsignedInt(ByteUtil.getIntLE(address() + absReadIndex()));
        this.skipRead(4);
        return v;
    }

    @Override
    public long getUnsignedIntLE(int index) {
        return toUnsignedInt(ByteUtil.getIntLE(address() + ix(index)));
    }

    @Override
    public int readUnsignedShort() {
        int v = ByteUtil.getShort(address() + absReadIndex()) & 0xffff;
        this.skipRead(2);
        return v;
    }

    @Override
    public int getUnsignedShort(int index) {
        return ByteUtil.getShort(address() + ix(index)) & 0xffff;
    }

    @Override
    public int readUnsignedShortLE() {
        int v = ByteUtil.getShortLE(address() + absReadIndex()) & 0xffff;
        this.skipRead(2);
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
    public int indexOf(byte b, int absFrom, int absTo) {
        long addr = address();
        if (BUF_FAST_INDEX_OF) {
            int size = absTo - absFrom;
            if (size < 16) {
                return unroll_index_of(addr, addr + absFrom, size, b);
            } else {
                return index_of_16(addr, absFrom, size, b);
            }
        }
        return plain_index_of(addr, addr + absFrom, addr + absTo, b);
    }

    private static int index_of_16(long addr, int from, int size, byte b) {
        int  group      = 16;
        int  count      = (size >>> 4) << 4;
        long addr_from  = addr + from;
        long addr_s1_to = addr_from + count;
        for (long i = addr_from; i < addr_s1_to; i += group) {
            if (Unsafe.getByte(i) == b) {
                return (int) (i - addr);
            }
            if (Unsafe.getByte(i + 1) == b) {
                return (int) (i + 1 - addr);
            }
            if (Unsafe.getByte(i + 2) == b) {
                return (int) (i + 2 - addr);
            }
            if (Unsafe.getByte(i + 3) == b) {
                return (int) (i + 3 - addr);
            }
            if (Unsafe.getByte(i + 4) == b) {
                return (int) (i + 4 - addr);
            }
            if (Unsafe.getByte(i + 5) == b) {
                return (int) (i + 5 - addr);
            }
            if (Unsafe.getByte(i + 6) == b) {
                return (int) (i + 6 - addr);
            }
            if (Unsafe.getByte(i + 7) == b) {
                return (int) (i + 7 - addr);
            }
            if (Unsafe.getByte(i + 8) == b) {
                return (int) (i + 8 - addr);
            }
            if (Unsafe.getByte(i + 9) == b) {
                return (int) (i + 9 - addr);
            }
            if (Unsafe.getByte(i + 10) == b) {
                return (int) (i + 10 - addr);
            }
            if (Unsafe.getByte(i + 11) == b) {
                return (int) (i + 11 - addr);
            }
            if (Unsafe.getByte(i + 12) == b) {
                return (int) (i + 12 - addr);
            }
            if (Unsafe.getByte(i + 13) == b) {
                return (int) (i + 13 - addr);
            }
            if (Unsafe.getByte(i + 14) == b) {
                return (int) (i + 14 - addr);
            }
            if (Unsafe.getByte(i + 15) == b) {
                return (int) (i + 15 - addr);
            }
        }
        return unroll_index_of(addr, addr_s1_to, size & (group - 1), b);
    }

    private static int unroll_index_of(long addr, long from, int size, byte b) {
        switch (size) {
            case 1:
                if (Unsafe.getByte(from) == b) {
                    return (int) (from - addr);
                }
                break;
            case 2:
                if (Unsafe.getByte(from) == b) {
                    return (int) (from - addr);
                }
                if (Unsafe.getByte(from + 1) == b) {
                    return (int) (from + 1 - addr);
                }
                break;
            case 3:
                if (Unsafe.getByte(from) == b) {
                    return (int) (from - addr);
                }
                if (Unsafe.getByte(from + 1) == b) {
                    return (int) (from + 1 - addr);
                }
                if (Unsafe.getByte(from + 2) == b) {
                    return (int) (from + 2 - addr);
                }
                break;
            case 4:
                if (Unsafe.getByte(from) == b) {
                    return (int) (from - addr);
                }
                if (Unsafe.getByte(from + 1) == b) {
                    return (int) (from + 1 - addr);
                }
                if (Unsafe.getByte(from + 2) == b) {
                    return (int) (from + 2 - addr);
                }
                if (Unsafe.getByte(from + 3) == b) {
                    return (int) (from + 3 - addr);
                }
                break;
            case 5:
                if (Unsafe.getByte(from) == b) {
                    return (int) (from - addr);
                }
                if (Unsafe.getByte(from + 1) == b) {
                    return (int) (from + 1 - addr);
                }
                if (Unsafe.getByte(from + 2) == b) {
                    return (int) (from + 2 - addr);
                }
                if (Unsafe.getByte(from + 3) == b) {
                    return (int) (from + 3 - addr);
                }
                if (Unsafe.getByte(from + 4) == b) {
                    return (int) (from + 4 - addr);
                }
                break;
            case 6:
                if (Unsafe.getByte(from) == b) {
                    return (int) (from - addr);
                }
                if (Unsafe.getByte(from + 1) == b) {
                    return (int) (from + 1 - addr);
                }
                if (Unsafe.getByte(from + 2) == b) {
                    return (int) (from + 2 - addr);
                }
                if (Unsafe.getByte(from + 3) == b) {
                    return (int) (from + 3 - addr);
                }
                if (Unsafe.getByte(from + 4) == b) {
                    return (int) (from + 4 - addr);
                }
                if (Unsafe.getByte(from + 5) == b) {
                    return (int) (from + 5 - addr);
                }
                break;
            case 7:
                if (Unsafe.getByte(from) == b) {
                    return (int) (from - addr);
                }
                if (Unsafe.getByte(from + 1) == b) {
                    return (int) (from + 1 - addr);
                }
                if (Unsafe.getByte(from + 2) == b) {
                    return (int) (from + 2 - addr);
                }
                if (Unsafe.getByte(from + 3) == b) {
                    return (int) (from + 3 - addr);
                }
                if (Unsafe.getByte(from + 4) == b) {
                    return (int) (from + 4 - addr);
                }
                if (Unsafe.getByte(from + 5) == b) {
                    return (int) (from + 5 - addr);
                }
                if (Unsafe.getByte(from + 6) == b) {
                    return (int) (from + 6 - addr);
                }
                break;
            case 8:
                if (Unsafe.getByte(from) == b) {
                    return (int) (from - addr);
                }
                if (Unsafe.getByte(from + 1) == b) {
                    return (int) (from + 1 - addr);
                }
                if (Unsafe.getByte(from + 2) == b) {
                    return (int) (from + 2 - addr);
                }
                if (Unsafe.getByte(from + 3) == b) {
                    return (int) (from + 3 - addr);
                }
                if (Unsafe.getByte(from + 4) == b) {
                    return (int) (from + 4 - addr);
                }
                if (Unsafe.getByte(from + 5) == b) {
                    return (int) (from + 5 - addr);
                }
                if (Unsafe.getByte(from + 6) == b) {
                    return (int) (from + 6 - addr);
                }
                if (Unsafe.getByte(from + 7) == b) {
                    return (int) (from + 7 - addr);
                }
                break;
            case 9:
                if (Unsafe.getByte(from) == b) {
                    return (int) (from - addr);
                }
                if (Unsafe.getByte(from + 1) == b) {
                    return (int) (from + 1 - addr);
                }
                if (Unsafe.getByte(from + 2) == b) {
                    return (int) (from + 2 - addr);
                }
                if (Unsafe.getByte(from + 3) == b) {
                    return (int) (from + 3 - addr);
                }
                if (Unsafe.getByte(from + 4) == b) {
                    return (int) (from + 4 - addr);
                }
                if (Unsafe.getByte(from + 5) == b) {
                    return (int) (from + 5 - addr);
                }
                if (Unsafe.getByte(from + 6) == b) {
                    return (int) (from + 6 - addr);
                }
                if (Unsafe.getByte(from + 7) == b) {
                    return (int) (from + 7 - addr);
                }
                if (Unsafe.getByte(from + 8) == b) {
                    return (int) (from + 8 - addr);
                }
                break;
            case 10:
                if (Unsafe.getByte(from) == b) {
                    return (int) (from - addr);
                }
                if (Unsafe.getByte(from + 1) == b) {
                    return (int) (from + 1 - addr);
                }
                if (Unsafe.getByte(from + 2) == b) {
                    return (int) (from + 2 - addr);
                }
                if (Unsafe.getByte(from + 3) == b) {
                    return (int) (from + 3 - addr);
                }
                if (Unsafe.getByte(from + 4) == b) {
                    return (int) (from + 4 - addr);
                }
                if (Unsafe.getByte(from + 5) == b) {
                    return (int) (from + 5 - addr);
                }
                if (Unsafe.getByte(from + 6) == b) {
                    return (int) (from + 6 - addr);
                }
                if (Unsafe.getByte(from + 7) == b) {
                    return (int) (from + 7 - addr);
                }
                if (Unsafe.getByte(from + 8) == b) {
                    return (int) (from + 8 - addr);
                }
                if (Unsafe.getByte(from + 9) == b) {
                    return (int) (from + 9 - addr);
                }
                break;
            case 11:
                if (Unsafe.getByte(from) == b) {
                    return (int) (from - addr);
                }
                if (Unsafe.getByte(from + 1) == b) {
                    return (int) (from + 1 - addr);
                }
                if (Unsafe.getByte(from + 2) == b) {
                    return (int) (from + 2 - addr);
                }
                if (Unsafe.getByte(from + 3) == b) {
                    return (int) (from + 3 - addr);
                }
                if (Unsafe.getByte(from + 4) == b) {
                    return (int) (from + 4 - addr);
                }
                if (Unsafe.getByte(from + 5) == b) {
                    return (int) (from + 5 - addr);
                }
                if (Unsafe.getByte(from + 6) == b) {
                    return (int) (from + 6 - addr);
                }
                if (Unsafe.getByte(from + 7) == b) {
                    return (int) (from + 7 - addr);
                }
                if (Unsafe.getByte(from + 8) == b) {
                    return (int) (from + 8 - addr);
                }
                if (Unsafe.getByte(from + 9) == b) {
                    return (int) (from + 9 - addr);
                }
                if (Unsafe.getByte(from + 10) == b) {
                    return (int) (from + 10 - addr);
                }
                break;
            case 12:

                if (Unsafe.getByte(from) == b) {
                    return (int) (from - addr);
                }
                if (Unsafe.getByte(from + 1) == b) {
                    return (int) (from + 1 - addr);
                }
                if (Unsafe.getByte(from + 2) == b) {
                    return (int) (from + 2 - addr);
                }
                if (Unsafe.getByte(from + 3) == b) {
                    return (int) (from + 3 - addr);
                }
                if (Unsafe.getByte(from + 4) == b) {
                    return (int) (from + 4 - addr);
                }
                if (Unsafe.getByte(from + 5) == b) {
                    return (int) (from + 5 - addr);
                }
                if (Unsafe.getByte(from + 6) == b) {
                    return (int) (from + 6 - addr);
                }
                if (Unsafe.getByte(from + 7) == b) {
                    return (int) (from + 7 - addr);
                }
                if (Unsafe.getByte(from + 8) == b) {
                    return (int) (from + 8 - addr);
                }
                if (Unsafe.getByte(from + 9) == b) {
                    return (int) (from + 9 - addr);
                }
                if (Unsafe.getByte(from + 10) == b) {
                    return (int) (from + 10 - addr);
                }
                if (Unsafe.getByte(from + 11) == b) {
                    return (int) (from + 11 - addr);
                }
                break;
            case 13:
                if (Unsafe.getByte(from) == b) {
                    return (int) (from - addr);
                }
                if (Unsafe.getByte(from + 1) == b) {
                    return (int) (from + 1 - addr);
                }
                if (Unsafe.getByte(from + 2) == b) {
                    return (int) (from + 2 - addr);
                }
                if (Unsafe.getByte(from + 3) == b) {
                    return (int) (from + 3 - addr);
                }
                if (Unsafe.getByte(from + 4) == b) {
                    return (int) (from + 4 - addr);
                }
                if (Unsafe.getByte(from + 5) == b) {
                    return (int) (from + 5 - addr);
                }
                if (Unsafe.getByte(from + 6) == b) {
                    return (int) (from + 6 - addr);
                }
                if (Unsafe.getByte(from + 7) == b) {
                    return (int) (from + 7 - addr);
                }
                if (Unsafe.getByte(from + 8) == b) {
                    return (int) (from + 8 - addr);
                }
                if (Unsafe.getByte(from + 9) == b) {
                    return (int) (from + 9 - addr);
                }
                if (Unsafe.getByte(from + 10) == b) {
                    return (int) (from + 10 - addr);
                }
                if (Unsafe.getByte(from + 11) == b) {
                    return (int) (from + 11 - addr);
                }
                if (Unsafe.getByte(from + 12) == b) {
                    return (int) (from + 12 - addr);
                }
                break;
            case 14:
                if (Unsafe.getByte(from) == b) {
                    return (int) (from - addr);
                }
                if (Unsafe.getByte(from + 1) == b) {
                    return (int) (from + 1 - addr);
                }
                if (Unsafe.getByte(from + 2) == b) {
                    return (int) (from + 2 - addr);
                }
                if (Unsafe.getByte(from + 3) == b) {
                    return (int) (from + 3 - addr);
                }
                if (Unsafe.getByte(from + 4) == b) {
                    return (int) (from + 4 - addr);
                }
                if (Unsafe.getByte(from + 5) == b) {
                    return (int) (from + 5 - addr);
                }
                if (Unsafe.getByte(from + 6) == b) {
                    return (int) (from + 6 - addr);
                }
                if (Unsafe.getByte(from + 7) == b) {
                    return (int) (from + 7 - addr);
                }
                if (Unsafe.getByte(from + 8) == b) {
                    return (int) (from + 8 - addr);
                }
                if (Unsafe.getByte(from + 9) == b) {
                    return (int) (from + 9 - addr);
                }
                if (Unsafe.getByte(from + 10) == b) {
                    return (int) (from + 10 - addr);
                }
                if (Unsafe.getByte(from + 11) == b) {
                    return (int) (from + 11 - addr);
                }
                if (Unsafe.getByte(from + 12) == b) {
                    return (int) (from + 12 - addr);
                }
                if (Unsafe.getByte(from + 13) == b) {
                    return (int) (from + 13 - addr);
                }
                break;
            case 15:
                if (Unsafe.getByte(from) == b) {
                    return (int) (from - addr);
                }
                if (Unsafe.getByte(from + 1) == b) {
                    return (int) (from + 1 - addr);
                }
                if (Unsafe.getByte(from + 2) == b) {
                    return (int) (from + 2 - addr);
                }
                if (Unsafe.getByte(from + 3) == b) {
                    return (int) (from + 3 - addr);
                }
                if (Unsafe.getByte(from + 4) == b) {
                    return (int) (from + 4 - addr);
                }
                if (Unsafe.getByte(from + 5) == b) {
                    return (int) (from + 5 - addr);
                }
                if (Unsafe.getByte(from + 6) == b) {
                    return (int) (from + 6 - addr);
                }
                if (Unsafe.getByte(from + 7) == b) {
                    return (int) (from + 7 - addr);
                }
                if (Unsafe.getByte(from + 8) == b) {
                    return (int) (from + 8 - addr);
                }
                if (Unsafe.getByte(from + 9) == b) {
                    return (int) (from + 9 - addr);
                }
                if (Unsafe.getByte(from + 10) == b) {
                    return (int) (from + 10 - addr);
                }
                if (Unsafe.getByte(from + 11) == b) {
                    return (int) (from + 11 - addr);
                }
                if (Unsafe.getByte(from + 12) == b) {
                    return (int) (from + 12 - addr);
                }
                if (Unsafe.getByte(from + 13) == b) {
                    return (int) (from + 13 - addr);
                }
                if (Unsafe.getByte(from + 14) == b) {
                    return (int) (from + 14 - addr);
                }
                break;

        }
        return -1;
    }

    private static int plain_index_of(long addr, long from, long to, byte b) {
        for (long i = from; i < to; i++) {
            if (Unsafe.getByte(i) == b) {
                return (int) (i - addr);
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(byte b, int absFrom, int absTo) {
        long addr = address();
        long p    = addr + absFrom;
        long l    = addr + absTo;
        for (; p > l; p--) {
            if (Unsafe.getByte(p) == b) {
                return (int) (p - addr);
            }
        }
        return -1;
    }

    @Override
    public void setByte(int index, byte b) {
        Unsafe.putByte(address() + ix(index), b);
    }

    @Override
    protected void writeByte0(byte b) {
        Unsafe.putByte(address() + abs_write_index++, b);
    }

    @Override
    protected int setBytes0(int index, byte[] src, int offset, int length) {
        Unsafe.copyFromArray(src, offset, address() + ix(index), length);
        return length;
    }

    @Override
    protected int setBytes0(int index, ByteBuf src, int len) {
        if (src.hasArray()) {
            copy(src.array(), src.absReadIndex(), address() + ix(index), len);
        } else {
            copy(src.address() + src.absReadIndex(), address() + ix(index), len);
        }
        src.skipRead(len);
        return len;
    }

    @Override
    protected int setBytes0(int index, ByteBuffer src, int len) {
        if (src.hasArray()) {
            copy(src.array(), src.position(), address() + ix(index), len);
        } else {
            copy(Unsafe.address(src) + src.position(), address() + ix(index), len);
        }
        src.position(src.position() + len);
        return len;
    }

    @Override
    public int setBytes0(int index, long address, int len) {
        copy(address, address() + ix(index), len);
        return len;
    }

    @Override
    public int writeBytes0(long address, int len) {
        copy(address, address() + absWriteIndex(), len);
        skipWrite(len);
        return len;
    }

    @Override
    protected int writeBytes0(byte[] src, int offset, int length) {
        Unsafe.copyFromArray(src, offset, address() + absWriteIndex(), length);
        skipWrite(length);
        return length;
    }

    @Override
    protected int writeBytes0(ByteBuf src, int len) {
        if (src.hasArray()) {
            copy(src.array(), src.absReadIndex(), address() + absWriteIndex(), len);
        } else {
            copy(src.address() + src.absReadIndex(), address() + absWriteIndex(), len);
        }
        src.skipRead(len);
        skipWrite(len);
        return len;
    }

    @Override
    protected int writeBytes0(ByteBuffer src, int len) {
        if (src.hasArray()) {
            copy(src.array(), src.position(), address() + absWriteIndex(), len);
        } else {
            copy(Unsafe.address(src) + src.position(), address() + absWriteIndex(), len);
        }
        src.position(src.position() + len);
        skipWrite(len);
        return len;
    }

    @Override
    public void setInt(int index, int value) {
        ByteUtil.putInt(address() + ix(index), value);
    }

    @Override
    protected void writeInt0(int value) {
        ByteUtil.putInt(address() + absWriteIndex(), value);
        skipWrite(4);
    }

    @Override
    public void setIntLE(int index, int value) {
        ByteUtil.putIntLE(address() + ix(index), value);
    }

    @Override
    protected void writeIntLE0(int value) {
        ByteUtil.putIntLE(address() + absWriteIndex(), value);
        skipWrite(4);
    }

    @Override
    public void setLong(int index, long value) {
        ByteUtil.putLong(address() + ix(index), value);
    }

    @Override
    protected void writeLong0(long value) {
        ByteUtil.putLong(address() + absWriteIndex(), value);
        skipWrite(8);
    }

    @Override
    public void setLongLE(int index, long value) {
        ByteUtil.putLongLE(address() + ix(index), value);
    }

    @Override
    protected void writeLongLE0(long value) {
        ByteUtil.putLongLE(address() + absWriteIndex(), value);
        skipWrite(8);
    }

    @Override
    public void setShort(int index, int value) {
        ByteUtil.putShort(address() + ix(index), (short) value);
    }

    @Override
    protected void writeShort0(int value) {
        ByteUtil.putShort(address() + absWriteIndex(), (short) value);
        skipWrite(2);
    }

    @Override
    public void setShortLE(int index, int value) {
        ByteUtil.putShortLE(address() + ix(index), (short) value);
    }

    @Override
    protected void writeShortLE0(int value) {
        ByteUtil.putShortLE(address() + absWriteIndex(), (short) value);
        skipWrite(2);
    }

    public ByteBuf reverseRead() {
        return this;
    }

    public ByteBuf reverseWrite() {
        return this;
    }

}
