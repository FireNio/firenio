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

import com.generallycloud.baseio.common.Releasable;

public interface ByteBuf extends ByteBufNew, Releasable {

    public abstract byte[] array();

    public abstract int capacity();

    public abstract ByteBuf clear();

    public abstract ByteBuf duplicate();

    public abstract ByteBuf flip();

    public abstract int forEachByte(ByteProcessor processor);

    public abstract int forEachByte(int index, int length, ByteProcessor processor);

    public abstract int forEachByteDesc(ByteProcessor processor);

    public abstract int forEachByteDesc(int index, int length, ByteProcessor processor);

    public abstract void get(byte[] dst);

    public abstract void get(byte[] dst, int offset, int length);

    public abstract byte getByte();

    public abstract byte getByte(int index);

    public abstract byte[] getBytes();

    public abstract int getInt();

    public abstract int getInt(int index);

    public abstract int getIntLE();

    public abstract int getIntLE(int index);

    public abstract long getLong();

    public abstract long getLong(int index);

    public abstract long getLongLE();

    public abstract long getLongLE(int index);

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

    public abstract int limit();

    public abstract ByteBuf limit(int limit);

    public abstract ByteBuffer getNioBuffer();

    public abstract ByteBuffer nioBuffer();

    public abstract int offset();

    public abstract int position();

    public abstract ByteBuf position(int position);

    public abstract void putByte(byte b);

    public abstract void put(byte[] src);

    public abstract void put(byte[] src, int offset, int length);

    public abstract void putShort(short value);

    public abstract void putShortLE(short value);

    public abstract void putUnsignedShort(int value);

    public abstract void putUnsignedShortLE(int value);

    public abstract void putInt(int value);

    public abstract void putIntLE(int value);

    public abstract void putUnsignedInt(long value);

    public abstract void putUnsignedIntLE(long value);

    public abstract void putLong(long value);

    public abstract void putLongLE(long value);

    public abstract int read(ByteBuf src);

    public abstract int read(ByteBuffer buffer);

    public abstract int remaining();

    public abstract ByteBuf reverse();

    public abstract ByteBuf reallocate(int limit);

    public abstract ByteBuf reallocate(int limit, boolean copyOld);

    public abstract ByteBuf reallocate(int limit, int maxLimit);

    public abstract ByteBuf reallocate(int limit, int maxLimit, boolean copyOld);

    public abstract ByteBuf skipBytes(int length);

}
