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

    static final boolean                            AUTO_EXPANSION;
    static final AtomicIntegerFieldUpdater<ByteBuf> refCntUpdater;

    static {
        AUTO_EXPANSION = Options.isBufAutoExpansion();
        refCntUpdater = AtomicIntegerFieldUpdater.newUpdater(ByteBuf.class, "referenceCount");
    }

    protected volatile int referenceCount = 0;

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

    public static ByteBuf direct(int cap) {
        return wrap(ByteBuffer.allocateDirect(cap));
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

    public abstract byte absByte(int pos);

    public abstract int absLimit();

    public abstract ByteBuf absLimit(int limit);

    public abstract int absPos();

    public abstract ByteBuf absPos(int absPos);

    protected void addReferenceCount() {
        int referenceCount = this.referenceCount;
        if (refCntUpdater.compareAndSet(this, referenceCount, referenceCount + 1)) {
            return;
        }
        for (; ; ) {
            referenceCount = this.referenceCount;
            if (refCntUpdater.compareAndSet(this, referenceCount, referenceCount + 1)) {
                break;
            }
        }
    }

    public abstract long address();

    public abstract byte[] array();

    public abstract int capacity();

    protected void capacity(int cap) {}

    public abstract ByteBuf clear();

    public abstract ByteBuf duplicate();

    final void ensureWritable(int len) {
        if (AUTO_EXPANSION && len > remaining()) {
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

    public abstract ByteBuf flip();

    protected abstract int get0(ByteBuffer dst, int len);

    public abstract byte getByte();

    public abstract byte getByte(int index);

    public byte[] getBytes() {
        return getBytes(remaining());
    }

    public void getBytes(byte[] dst) {
        getBytes(dst, 0, dst.length);
    }

    public abstract void getBytes(byte[] dst, int offset, int length);

    public int getBytes(ByteBuf dst) {
        return dst.putBytes(this, dst.remaining());
    }

    public int getBytes(ByteBuf dst, int length) {
        return dst.putBytes(this, length);
    }

    public int getBytes(ByteBuffer dst) {
        int len = Math.min(remaining(), dst.remaining());
        if (len == 0) {
            return 0;
        }
        return get0(dst, len);
    }

    public int getBytes(ByteBuffer dst, int length) {
        int len = Math.min(remaining(), dst.remaining());
        len = Math.min(len, length);
        if (len == 0) {
            return 0;
        }
        return get0(dst, len);
    }

    public byte[] getBytes(int length) {
        byte[] bytes = new byte[length];
        getBytes(bytes);
        return bytes;
    }

    public abstract int getInt();

    public abstract int getInt(int index);

    public abstract int getIntLE();

    public abstract int getIntLE(int index);

    public abstract long getLong();

    public abstract long getLong(int index);

    public abstract long getLongLE();

    public abstract long getLongLE(int index);

    public float getFloat() {
        return Float.intBitsToFloat(getInt());
    }

    public float getFloat(int index) {
        return Float.intBitsToFloat(getInt(index));
    }

    public float getFloatLE() {
        return Float.intBitsToFloat(getIntLE());
    }

    public float getFloatLE(int index) {
        return Float.intBitsToFloat(getIntLE(index));
    }

    public double getDouble() {
        return Double.longBitsToDouble(getLong());
    }

    public double getDouble(int index) {
        return Double.longBitsToDouble(getLong(index));
    }

    public double getDoubleLE() {
        return Double.longBitsToDouble(getLongLE());
    }

    public double getDoubleLE(int index) {
        return Double.longBitsToDouble(getLongLE(index));
    }

    public abstract ByteBuffer getNioBuffer();

    public abstract short getShort();

    public abstract short getShort(int index);

    public abstract short getShortLE();

    public abstract short getShortLE(int index);

    public abstract short getUnsignedByte();

    public abstract short getUnsignedByte(int index);

    public abstract long getUnsignedInt();

    public abstract long getUnsignedInt(int index);

    public abstract long getUnsignedIntLE();

    public abstract long getUnsignedIntLE(int index);

    public abstract int getUnsignedShort();

    public abstract int getUnsignedShort(int index);

    public abstract int getUnsignedShortLE();

    public abstract int getUnsignedShortLE(int index);

    public abstract boolean hasArray();

    public abstract boolean hasRemaining();

    public int indexOf(byte b) {
        return indexOf(b, absPos(), remaining());
    }

    public int indexOf(byte b, int size) {
        return indexOf(b, absPos(), size);
    }

    public abstract int indexOf(byte b, int absPos, int size);

    public abstract boolean isPooled();

    @Override
    public boolean isReleased() {
        return referenceCount < 1;
    }

    protected int ix(int index) {
        return offset() + index;
    }

    public int lastIndexOf(byte b) {
        return lastIndexOf(b, absLimit() - 1, remaining());
    }

    public int lastIndexOf(byte b, int size) {
        return lastIndexOf(b, absLimit() - 1, size);
    }

    public abstract int lastIndexOf(byte b, int absPos, int size);

    //---------------------------------put byte---------------------------------//

    public abstract int limit();

    public abstract ByteBuf limit(int limit);

    public abstract ByteBuf markL();

    //---------------------------------put bytes---------------------------------//

    public abstract ByteBuf markP();

    public abstract ByteBuffer nioBuffer();

    protected int offset() {
        return 0;
    }

    protected void offset(int offset) {}

    public abstract int position();

    public abstract ByteBuf position(int position);

    protected ByteBuf produce(int unitOffset, int unitEnd) {
        return this;
    }

    public void putByte(byte b) {
        ensureWritable(1);
        putByte0(b);
    }

    public abstract void putByte(int index, byte b);

    protected abstract void putByte0(byte b);

    public void putBytes(byte[] src) {
        putBytes(src, 0, src.length);
    }

    //---------------------------------put int---------------------------------//

    public int putBytes(byte[] src, int offset, int length) {
        if (AUTO_EXPANSION) {
            ensureWritable(length);
            return putBytes0(src, offset, length);
        } else {
            if (!hasRemaining()) {
                return 0;
            }
            return putBytes0(src, offset, Math.min(remaining(), length));
        }
    }

    public int putBytes(ByteBuf src) {
        int len = src.remaining();
        if (len == 0) {
            return 0;
        }
        return putBytes0(src, len);
    }

    public int putBytes(ByteBuf src, int length) {
        int len = Math.min(length, src.remaining());
        if (len == 0) {
            return 0;
        }
        return putBytes0(src, len);
    }

    public int putBytes(ByteBuffer src) {
        int len = src.remaining();
        if (len == 0) {
            return 0;
        }
        return putBytes0(src, len);
    }

    public int putBytes(ByteBuffer src, int length) {
        int len = Math.min(length, src.remaining());
        if (len == 0) {
            return 0;
        }
        return putBytes0(src, len);
    }

    protected abstract int putBytes0(byte[] src, int offset, int length);

    //---------------------------------put long---------------------------------//

    protected int putBytes0(ByteBuf src, int len) {
        if (AUTO_EXPANSION) {
            ensureWritable(len);
            return putBytes00(src, len);
        } else {
            if (!hasRemaining()) {
                return 0;
            }
            return putBytes00(src, Math.min(remaining(), len));
        }
    }

    protected int putBytes0(ByteBuffer src, int len) {
        if (AUTO_EXPANSION) {
            ensureWritable(len);
            return putBytes00(src, len);
        } else {
            if (!hasRemaining()) {
                return 0;
            }
            return putBytes00(src, Math.min(remaining(), len));
        }
    }

    protected abstract int putBytes00(ByteBuf src, int len);

    protected abstract int putBytes00(ByteBuffer src, int len);

    public void putInt(int value) {
        ensureWritable(4);
        putInt0(value);
    }

    public abstract void putInt(int index, int value);

    //---------------------------------put double---------------------------------//

    protected abstract void putInt0(int value);

    public void putIntLE(int value) {
        ensureWritable(4);
        putIntLE0(value);
    }

    public abstract void putIntLE(int index, int value);

    protected abstract void putIntLE0(int value);

    //---------------------------------put float---------------------------------//

    public abstract void putLong(int index, long value);

    public void putLong(long value) {
        ensureWritable(8);
        putLong0(value);
    }

    protected abstract void putLong0(long value);

    public abstract void putLongLE(int index, long value);

    //---------------------------------put short---------------------------------//

    public void putLongLE(long value) {
        ensureWritable(8);
        putLongLE0(value);
    }

    protected abstract void putLongLE0(long value);

    public void putDouble(int index, double value) {
        putLong(index, Double.doubleToRawLongBits(value));
    }

    public void putDouble(double value) {
        putLong(Double.doubleToRawLongBits(value));
    }

    public void putDoubleLE(int index, double value) {
        putLongLE(index, Double.doubleToRawLongBits(value));
    }

    public void putDoubleLE(double value) {
        putLongLE(Double.doubleToRawLongBits(value));
    }

    public void putFloat(int index, float value) {
        putInt(index, Float.floatToRawIntBits(value));
    }

    public void putFloat(float value) {
        putInt(Float.floatToRawIntBits(value));
    }

    public void putFloatLE(int index, float value) {
        putIntLE(index, Float.floatToRawIntBits(value));
    }

    public void putFloatLE(float value) {
        putIntLE(Float.floatToRawIntBits(value));
    }

    public void putShort(int value) {
        ensureWritable(2);
        putShort0(value);
    }

    public abstract void putShort(int index, int value);

    protected abstract void putShort0(int value);

    public void putShortLE(int value) {
        ensureWritable(2);
        putShortLE0(value);
    }

    public abstract void putShortLE(int index, int value);

    protected abstract void putShortLE0(int value);

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
            }
        }
    }

    protected abstract void release0();

    public abstract int remaining();

    public abstract ByteBuf resetL();

    public abstract ByteBuf resetP();

    public abstract ByteBuf reverse();

    public abstract ByteBuf skip(int length);

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(getClass().getSimpleName());
        b.append("[pos=");
        b.append(position());
        b.append(",lim=");
        b.append(limit());
        b.append(",cap=");
        b.append(capacity());
        b.append(",remaining=");
        b.append(remaining());
        b.append(",offset=");
        b.append(offset());
        b.append("]");
        return b.toString();
    }

    protected int unitOffset() {
        return -1;

    }

    protected void unitOffset(int unitOffset) {}

}
