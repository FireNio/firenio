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

import com.generallycloud.baseio.Releasable;

public interface ByteBuf extends ByteBufBuilder, Releasable {

    int absLimit();

    int absPos();

    byte[] array();

    int capacity();

    ByteBuf clear();
    
    ByteBuf duplicate();
    
    ByteBuf flip();

    int forEachByte(ByteProcessor processor);

    int forEachByte(int index, int length, ByteProcessor processor);

    int forEachByteDesc(ByteProcessor processor);

    int forEachByteDesc(int index, int length, ByteProcessor processor);

    void get(byte[] dst);

    void get(byte[] dst, int offset, int length);

    byte getByte();

    byte getByte(int index);

    byte[] getBytes();

    int getInt();

    int getInt(int index);

    int getIntLE();

    int getIntLE(int index);

    long getLong();

    long getLong(int index);

    long getLongLE();

    long getLongLE(int index);

    ByteBuffer getNioBuffer();

    short getShort();

    short getShort(int index);

    short getShortLE();

    short getShortLE(int index);

    short getUnsignedByte();

    short getUnsignedByte(int index);

    long getUnsignedInt();

    long getUnsignedInt(int index);

    long getUnsignedIntLE();

    long getUnsignedIntLE(int index);

    int getUnsignedShort();

    int getUnsignedShort(int index);

    int getUnsignedShortLE();

    int getUnsignedShortLE(int index);

    boolean hasArray();

    boolean hasRemaining();

    int limit();

    ByteBuf limit(int limit);

    ByteBuf markL();

    ByteBuf markP();

    ByteBuffer nioBuffer();

    int offset();

    int position();

    ByteBuf position(int position);

    void put(byte[] src);

    void put(byte[] src, int offset, int length);

    void putByte(byte b);

    void putInt(int value);

    void putIntLE(int value);

    void putLong(long value);

    void putLongLE(long value);

    void putShort(short value);

    void putShortLE(short value);

    void putUnsignedInt(long value);

    void putUnsignedIntLE(long value);

    void putUnsignedShort(int value);

    void putUnsignedShortLE(int value);

    int read(ByteBuf src);

    int read(ByteBuf src, int length);

    int read(ByteBuffer src);

    int read(ByteBuffer src, int length);

    ByteBuf reallocate(int limit);

    ByteBuf reallocate(int limit, boolean copyOld);

    ByteBuf reallocate(int limit, int maxLimit);

    ByteBuf reallocate(int limit, int maxLimit, boolean copyOld);

    int remaining();

    ByteBuf resetL();
    
    ByteBuf resetP();

    ByteBuf reverse();

    ByteBuf skip(int length);

}
