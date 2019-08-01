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
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import com.firenio.Options;
import com.firenio.Releasable;
import com.firenio.common.Unsafe;

public abstract class ByteBuf implements Releasable {

    static final boolean                            BUF_THREAD_YIELD;
    static final boolean                            AUTO_EXPANSION;
    static final AtomicIntegerFieldUpdater<ByteBuf> refCntUpdater;

    static {
        BUF_THREAD_YIELD = Options.isBufThreadYield();
        AUTO_EXPANSION = Options.isBufAutoExpansion();
        refCntUpdater = AtomicIntegerFieldUpdater.newUpdater(ByteBuf.class, "referenceCount");
    }

    protected volatile int referenceCount = 0;

    protected int offset;
    protected int abs_write_index;
    protected int abs_read_index;
    protected int marked_abs_write_index;
    protected int marked_abs_read_index;

    public static ByteBuf direct(int cap) {
        return wrap(Unsafe.allocateDirectByteBuffer(cap));
    }

    public static ByteBuf buffer(int cap) {
        if (Unsafe.UNSAFE_BUF_AVAILABLE) {
            return unsafe(cap);
        } else {
            if (Unsafe.DIRECT_BUFFER_AVAILABLE) {
                return direct(cap);
            } else {
                return heap(cap);
            }
        }
    }

    public static ByteBuf unsafe(int cap) {
        return new UnpooledUnsafeByteBuf(Unsafe.allocate(cap), cap);
    }

    public static ByteBuf wrapAuto(byte[] data) {
        return wrapAuto(data, 0, data.length);
    }

    public static ByteBuf wrapAuto(byte[] data, int off, int len) {
        if (preferHeap()) {
            return wrap(data, off, len);
        }
        ByteBuf buf = buffer(len);
        buf.writeBytes(data, off, len);
        return buf;
    }

    public static ByteBuf wrapUnsafe(byte[] data) {
        return wrapUnsafe(data, 0, data.length);
    }

    public static ByteBuf wrapUnsafe(byte[] data, int off, int len) {
        ByteBuf buf = unsafe(len);
        buf.writeBytes(data, off, len);
        return buf;
    }

    public static ByteBuf empty() {
        return UnpooledHeapByteBuf.EmptyByteBuf.EMPTY;
    }

    public static ByteBuf heap(int cap) {
        return wrap(new byte[cap]);
    }

    public static ByteBuf wrap(byte[] data) {
        return wrap(data, 0, data.length);
    }

    public static ByteBuf wrap(byte[] data, int offset, int length) {
        return new UnpooledHeapByteBuf(data, offset, length);
    }

    public static ByteBuf wrap(ByteBuffer buffer) {
        if (buffer.isDirect()) {
            return new UnpooledDirectByteBuf(buffer);
        } else {
            return new UnpooledHeapByteBuf(buffer);
        }
    }

    public abstract byte getByteAbs(int pos);

    public int absWriteIndex() {
        return abs_write_index;
    }

    public ByteBuf absWriteIndex(int index) {
        this.abs_write_index = index;
        return this;
    }

    public int absReadIndex() {
        return abs_read_index;
    }

    public ByteBuf absReadIndex(int index) {
        this.abs_read_index = index;
        return this;
    }

    protected void addReferenceCount() {
        int referenceCount = this.referenceCount;
        if (refCntUpdater.compareAndSet(this, referenceCount, referenceCount + 1)) {
            return;
        }
        for (; ; ) {
            referenceCount = this.referenceCount;
            if (refCntUpdater.compareAndSet(this, referenceCount, referenceCount + 1)) {
                break;
            } else {
                if (BUF_THREAD_YIELD) {
                    Thread.yield();
                }
            }
        }
    }

    public abstract long address();

    public abstract byte[] array();

    public abstract int capacity();

    protected void capacity(int cap) {}

    public ByteBuf clear() {
        abs_read_index = 0;
        abs_write_index = 0;
        return this;
    }

    public abstract ByteBuf duplicate();

    final void ensureWritable(int len) {
        if (AUTO_EXPANSION && len > writableBytes()) {
            int cap     = capacity();
            int wantCap = capacity() + len;
            int newCap  = cap + (cap >> 1);
            for (; newCap < wantCap; ) {
                newCap = newCap + (newCap >> 1);
            }
            expansion(newCap);
        }
    }

    public abstract void expansion(int cap);

    protected abstract int get0(ByteBuffer dst, int len);

    public abstract byte readByte();

    public abstract byte getByte(int index);

    public byte[] readBytes() {
        return readBytes(readableBytes());
    }

    public void readBytes(byte[] dst) {
        readBytes(dst, 0, dst.length);
    }

    public abstract void readBytes(byte[] dst, int offset, int length);

    public int readBytes(ByteBuf dst) {
        return dst.writeBytes(this, dst.writableBytes());
    }

    public int readBytes(ByteBuf dst, int length) {
        return dst.writeBytes(this, length);
    }

    public int readBytes(ByteBuffer dst) {
        int len = Math.min(readableBytes(), dst.remaining());
        if (len == 0) {
            return 0;
        }
        return get0(dst, len);
    }

    public int readBytes(ByteBuffer dst, int length) {
        int len = Math.min(readableBytes(), dst.remaining());
        len = Math.min(len, length);
        if (len == 0) {
            return 0;
        }
        return get0(dst, len);
    }

    public byte[] readBytes(int length) {
        byte[] bytes = new byte[length];
        readBytes(bytes);
        return bytes;
    }

    public abstract int readInt();

    public abstract int getInt(int index);

    public abstract int readIntLE();

    public abstract int getIntLE(int index);

    public abstract long readLong();

    public abstract long getLong(int index);

    public abstract long readLongLE();

    public abstract long getLongLE(int index);

    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    public float getFloat(int index) {
        return Float.intBitsToFloat(getInt(index));
    }

    public float readFloatLE() {
        return Float.intBitsToFloat(readIntLE());
    }

    public float getFloatLE(int index) {
        return Float.intBitsToFloat(getIntLE(index));
    }

    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    public double getDouble(int index) {
        return Double.longBitsToDouble(getLong(index));
    }

    public double readDoubleLE() {
        return Double.longBitsToDouble(readLongLE());
    }

    public double getDoubleLE(int index) {
        return Double.longBitsToDouble(getLongLE(index));
    }

    protected abstract ByteBuffer getNioBuffer();

    public abstract short readShort();

    public abstract short getShort(int index);

    public abstract short readShortLE();

    public abstract short getShortLE(int index);

    public abstract short readUnsignedByte();

    public abstract short getUnsignedByte(int index);

    public abstract long readUnsignedInt();

    public abstract long getUnsignedInt(int index);

    public abstract long readUnsignedIntLE();

    public abstract long getUnsignedIntLE(int index);

    public abstract int readUnsignedShort();

    public abstract int getUnsignedShort(int index);

    public abstract int readUnsignedShortLE();

    public abstract int getUnsignedShortLE(int index);

    public abstract boolean hasArray();

    public boolean hasReadableBytes() {
        return abs_read_index < abs_write_index;
    }

    public int indexOf(byte b) {
        return indexOf(b, absReadIndex(), readableBytes());
    }

    public int indexOf(byte b, int size) {
        return indexOf(b, absReadIndex(), size);
    }

    public abstract int indexOf(byte b, int absPos, int size);

    public boolean hasWritableBytes() {
        return writableBytes() > 0;
    }

    public abstract boolean isPooled();

    @Override
    public boolean isReleased() {
        return referenceCount < 1;
    }

    protected int ix(int index) {
        return offset() + index;
    }

    public int lastIndexOf(byte b) {
        return lastIndexOf(b, absWriteIndex() - 1, readableBytes());
    }

    public int lastIndexOf(byte b, int size) {
        return lastIndexOf(b, absWriteIndex() - 1, size);
    }

    public abstract int lastIndexOf(byte b, int absPos, int size);

    public int writeIndex() {
        return abs_write_index - offset();
    }

    public ByteBuf writeIndex(int index) {
        this.abs_write_index = ix(index);
        return this;
    }

    public ByteBuf markWriteIndex() {
        this.marked_abs_write_index = this.abs_write_index;
        return this;
    }

    public ByteBuf markReadIndex() {
        this.marked_abs_read_index = this.abs_read_index;
        return this;
    }

    protected int offset() {
        return offset;
    }

    protected void offset(int offset) {
        this.offset = offset;
    }

    public int readIndex() {
        return abs_read_index - offset();
    }

    public ByteBuf readIndex(int index) {
        this.abs_read_index = ix(index);
        return this;
    }

    protected ByteBuf produce(int unitOffset, int unitEnd) {
        return this;
    }

    public void writeByte(byte b) {
        ensureWritable(1);
        writeByte0(b);
    }

    public abstract void setByte(int index, byte b);

    protected abstract void writeByte0(byte b);

    public void writeBytes(byte[] src) {
        writeBytes(src, 0, src.length);
    }

    //---------------------------------put int---------------------------------//

    public int writeBytes(byte[] src, int offset, int length) {
        if (AUTO_EXPANSION) {
            ensureWritable(length);
            return writeBytes0(src, offset, length);
        } else {
            if (!hasWritableBytes()) {
                return 0;
            }
            return writeBytes0(src, offset, Math.min(writableBytes(), length));
        }
    }

    public int writeBytes(ByteBuf src) {
        int len = src.readableBytes();
        if (len == 0) {
            return 0;
        }
        return writeBytes0(src, len);
    }

    public int writeBytes(ByteBuf src, int length) {
        int len = Math.min(length, src.readableBytes());
        if (len == 0) {
            return 0;
        }
        return writeBytes0(src, len);
    }

    public int writeBytes(ByteBuffer src) {
        int len = src.remaining();
        if (len == 0) {
            return 0;
        }
        return writeBytes0(src, len);
    }

    public int writeBytes(ByteBuffer src, int length) {
        int len = Math.min(length, src.remaining());
        if (len == 0) {
            return 0;
        }
        return writeBytes0(src, len);
    }

    protected abstract int writeBytes0(byte[] src, int offset, int length);

    //---------------------------------put long---------------------------------//

    protected int writeBytes0(ByteBuf src, int len) {
        if (AUTO_EXPANSION) {
            ensureWritable(len);
            return writeBytes00(src, len);
        } else {
            if (!hasWritableBytes()) {
                return 0;
            }
            return writeBytes00(src, Math.min(writableBytes(), len));
        }
    }

    protected int writeBytes0(ByteBuffer src, int len) {
        if (AUTO_EXPANSION) {
            ensureWritable(len);
            return writeBytes00(src, len);
        } else {
            if (!hasWritableBytes()) {
                return 0;
            }
            return writeBytes00(src, Math.min(writableBytes(), len));
        }
    }

    protected abstract int writeBytes00(ByteBuf src, int len);

    protected abstract int writeBytes00(ByteBuffer src, int len);

    public void writeInt(int value) {
        ensureWritable(4);
        writeInt0(value);
    }

    public abstract void setInt(int index, int value);

    //---------------------------------put double---------------------------------//

    protected abstract void writeInt0(int value);

    public void writeIntLE(int value) {
        ensureWritable(4);
        writeIntLE0(value);
    }

    public abstract void setIntLE(int index, int value);

    protected abstract void writeIntLE0(int value);

    public abstract void setLong(int index, long value);

    public void writeLong(long value) {
        ensureWritable(8);
        writeLong0(value);
    }

    protected abstract void writeLong0(long value);

    public abstract void setLongLE(int index, long value);

    public void writeLongLE(long value) {
        ensureWritable(8);
        writeLongLE0(value);
    }

    protected abstract void writeLongLE0(long value);

    public void setDouble(int index, double value) {
        setLong(index, Double.doubleToRawLongBits(value));
    }

    public void writeDouble(double value) {
        writeLong(Double.doubleToRawLongBits(value));
    }

    public void setDoubleLE(int index, double value) {
        setLongLE(index, Double.doubleToRawLongBits(value));
    }

    public void writeDoubleLE(double value) {
        writeLongLE(Double.doubleToRawLongBits(value));
    }

    public void setFloat(int index, float value) {
        setInt(index, Float.floatToRawIntBits(value));
    }

    public void writeFloat(float value) {
        writeInt(Float.floatToRawIntBits(value));
    }

    public void setFloatLE(int index, float value) {
        setIntLE(index, Float.floatToRawIntBits(value));
    }

    public void writeFloatLE(float value) {
        writeIntLE(Float.floatToRawIntBits(value));
    }

    public void writeShort(int value) {
        ensureWritable(2);
        writeShort0(value);
    }

    public abstract void setShort(int index, int value);

    protected abstract void writeShort0(int value);

    public void writeShortLE(int value) {
        ensureWritable(2);
        writeShortLE0(value);
    }

    public abstract void setShortLE(int index, int value);

    protected abstract void writeShortLE0(int value);

    @Override
    public final void release() {
        int referenceCount = this.referenceCount;
        if (referenceCount < 1) {
            return;
        }
        if (refCntUpdater.compareAndSet(this, referenceCount, referenceCount - 1)) {
            if (referenceCount == 1) {
                release0();
            }
            return;
        }
        for (; ; ) {
            referenceCount = this.referenceCount;
            if (referenceCount < 1) {
                return;
            }
            if (refCntUpdater.compareAndSet(this, referenceCount, referenceCount - 1)) {
                if (referenceCount == 1) {
                    release0();
                }
                return;
            } else {
                if (BUF_THREAD_YIELD) {
                    Thread.yield();
                }
            }
        }
    }

    public abstract ByteBuf reverseRead();

    public abstract ByteBuf reverseWrite();

    public ByteBuffer nioReadBuffer() {
        ByteBuffer buffer = getNioBuffer();
        return (ByteBuffer) buffer.limit(abs_write_index).position(abs_read_index);
    }

    public ByteBuffer nioWriteBuffer() {
        ByteBuffer buffer = getNioBuffer();
        return (ByteBuffer) buffer.limit(capacity() + offset()).position(abs_write_index);
    }

    protected abstract void release0();

    public int readableBytes() {
        return abs_write_index - abs_read_index;
    }

    public int writableBytes() {
        return capacity() - writeIndex();
    }

    public ByteBuf resetWriteIndex() {
        this.abs_write_index = marked_abs_write_index;
        return this;
    }

    public ByteBuf resetReadIndex() {
        this.abs_read_index = marked_abs_read_index;
        return this;
    }

    public int getMarkedAbsReadIndex() {
        return marked_abs_read_index;
    }

    public int getMarkedAbsWriteIndex() {
        return marked_abs_write_index;
    }

    public ByteBuf skipRead(int length) {
        this.abs_read_index += length;
        return this;
    }

    public ByteBuf skipWrite(int length) {
        this.abs_write_index += length;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(getClass().getSimpleName());
        b.append("[r=");
        b.append(readIndex());
        b.append(",w=");
        b.append(writeIndex());
        b.append(",c=");
        b.append(capacity());
        b.append(",ra=");
        b.append(readableBytes());
        b.append(",off=");
        b.append(offset());
        b.append("]");
        return b.toString();
    }

    protected int unitOffset() {
        return -1;
    }

    protected void unitOffset(int unitOffset) {}

    UnsupportedOperationException unsupportedOperationException() {
        return new UnsupportedOperationException();
    }

    public static boolean preferUnsafe() {
        return Unsafe.UNSAFE_BUF_AVAILABLE;
    }

    public static boolean preferDirect() {
        return !preferUnsafe() && Unsafe.DIRECT_BUFFER_AVAILABLE;
    }

    public static boolean preferHeap() {
        return !preferUnsafe() && !Unsafe.DIRECT_BUFFER_AVAILABLE;
    }

    static void copy(byte[] src, int srcPos, byte[] dst, int dstPos, int len) {
        System.arraycopy(src, srcPos, dst, dstPos, len);
    }

    static void copy(byte[] src, int srcPos, long dst, int len) {
        Unsafe.copyFromArray(src, srcPos, dst, len);
    }

    static void copy(long src, byte[] dst, int dstPos, int len) {
        Unsafe.copyToArray(src, dst, dstPos, len);
    }

    static void copy(long src, long dst, int len) {
        Unsafe.copyMemory(src, dst, len);
    }

    static long toUnsignedInt(int value) {
        if (value < 0) {
            return value & 0xffffffffffffffffL;
        }
        return value;
    }

}
