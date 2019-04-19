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
package com.firenio.common;

import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

/**
 * @author wangkai
 */
@SuppressWarnings("restriction")
public class Unsafe {

    public static final  long            ARRAY_BASE_OFFSET;
    public static final  long            BUFFER_ADDRESS_OFFSET;
    public static final  boolean         ENABLE;
    public static final  boolean         HAS_UNSAFE_ARRAY_OPERATIONS;
    public static final  boolean         HAS_UNSAFE_BYTE_BUFFER_OPERATIONS;
    // This number limits the number of bytes to copy per call to Unsafe's
    // copyMemory method. A limit is imposed to allow for safe point polling
    // during a large copy
    static final long                    UNSAFE_COPY_THRESHOLD = 1024L * 1024L;
    private static final ByteOrder       nativeByteOrder;
    private static final sun.misc.Unsafe UNSAFE;

    static {
        UNSAFE = getUnsafe();
        ENABLE = UNSAFE != null;
        BUFFER_ADDRESS_OFFSET = fieldOffset(field(Buffer.class, "address"));
        HAS_UNSAFE_BYTE_BUFFER_OPERATIONS = supportsUnsafeByteBufferOperations();
        HAS_UNSAFE_ARRAY_OPERATIONS = supportsUnsafeArrayOperations();
        ARRAY_BASE_OFFSET = byteArrayBaseOffset();
        nativeByteOrder = detectByteOrder();
    }

    private Unsafe() {}

    public static long address(ByteBuffer buffer) {
        return UNSAFE.getLong(buffer, BUFFER_ADDRESS_OFFSET);
    }

    public static long allocate(long length) {
        return UNSAFE.allocateMemory(length);
    }

    public static Object allocateInstance(Class<?> clazz) {
        try {
            return UNSAFE.allocateInstance(clazz);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    private static int byteArrayBaseOffset() {
        return HAS_UNSAFE_ARRAY_OPERATIONS ? UNSAFE.arrayBaseOffset(byte[].class) : -1;
    }

    public static boolean compareAndSwapInt(Object o, long offset, int expected, int val) {
        return UNSAFE.compareAndSwapInt(o, offset, expected, val);
    }

    public static boolean compareAndSwapLong(Object o, long offset, int expected, long val) {
        return UNSAFE.compareAndSwapLong(o, offset, expected, val);
    }

    public static boolean compareAndSwapObject(Object o, long offset, Object expected, Object val) {
        return UNSAFE.compareAndSwapObject(o, offset, expected, val);
    }

    /**
     * Copy from given source array to destination address.
     *
     * @param src           source array
     * @param srcPos        offset within source array of the first element to native_read
     * @param dstAddr       destination address
     * @param length        number of bytes to copy
     */
    public static void copyFromArray(byte[] src, long srcPos, long dstAddr, long length) {
        long offset = ARRAY_BASE_OFFSET + srcPos;
        while (length > 0) {
            long size = (length > UNSAFE_COPY_THRESHOLD) ? UNSAFE_COPY_THRESHOLD : length;
            UNSAFE.copyMemory(src, offset, null, dstAddr, size);
            length -= size;
            offset += size;
            dstAddr += size;
        }
    }

    public static void copyFromArray(ByteBuffer buf, long dstAddr, long length) {
        copyFromArray(buf.array(), buf.position(), dstAddr, length);
    }

    public static void copyMemory(ByteBuffer buf, long targetAddress, long length) {
        UNSAFE.copyMemory(address(buf) + buf.position(), targetAddress, length);
    }

    public static void copyMemory(long srcAddress, long targetAddress, long length) {
        UNSAFE.copyMemory(srcAddress, targetAddress, length);
    }

    public static void copyMemory(Object src, long srcOffset, Object target, long targetOffset, long length) {
        UNSAFE.copyMemory(src, srcOffset, target, targetOffset, length);
    }

    /**
     * Copy from source address into given destination array.
     *
     * @param srcAddr       source address
     * @param dst           destination array
     * @param dstPos        offset within destination array of the first element to write
     * @param length        number of bytes to copy
     */
    public static void copyToArray(long srcAddr, Object dst, long dstPos, long length) {
        long offset = ARRAY_BASE_OFFSET + dstPos;
        while (length > 0) {
            long size = (length > UNSAFE_COPY_THRESHOLD) ? UNSAFE_COPY_THRESHOLD : length;
            UNSAFE.copyMemory(null, srcAddr, dst, offset, size);
            length -= size;
            srcAddr += size;
            offset += size;
        }
    }

    private static ByteOrder detectByteOrder() {
        long a = UNSAFE.allocateMemory(8);
        try {
            UNSAFE.putLong(a, 0x0102030405060708L);
            byte b = UNSAFE.getByte(a);
            switch (b) {
                case 0x01:
                    return ByteOrder.BIG_ENDIAN;
                case 0x08:
                    return ByteOrder.LITTLE_ENDIAN;
                default:
                    assert false;
                    return null;
            }
        } finally {
            UNSAFE.freeMemory(a);
        }
    }

    private static Field field(Class<?> clazz, String fieldName) {
        Field field;
        try {
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
        } catch (Throwable t) {
            // Failed to access the fields.
            field = null;
        }
        return field;
    }

    private static long fieldOffset(Field field) {
        return field == null || UNSAFE == null ? -1 : UNSAFE.objectFieldOffset(field);
    }

    public static void free(long address) {
        UNSAFE.freeMemory(address);
    }

    public static long getArrayBaseOffset() {
        return ARRAY_BASE_OFFSET;
    }

    public static boolean getBoolean(Object target, long offset) {
        return UNSAFE.getBoolean(target, offset);
    }

    public static byte getByte(long address) {
        return UNSAFE.getByte(address);
    }

    public static byte getByte(Object target, long offset) {
        return UNSAFE.getByte(target, offset);
    }

    public static double getDouble(Object target, long offset) {
        return UNSAFE.getDouble(target, offset);
    }

    public static float getFloat(Object target, long offset) {
        return UNSAFE.getFloat(target, offset);
    }

    public static int getInt(long address) {
        return UNSAFE.getInt(address);
    }

    public static int getInt(Object target, long offset) {
        return UNSAFE.getInt(target, offset);
    }

    public static long getLong(long address) {
        return UNSAFE.getLong(address);
    }

    public static long getLong(Object target, long offset) {
        return UNSAFE.getLong(target, offset);
    }

    public static Object getObject(Object target, long offset) {
        return UNSAFE.getObject(target, offset);
    }

    public static short getShort(long address) {
        return UNSAFE.getShort(address);
    }

    public static short getShort(Object target, long offset) {
        return UNSAFE.getShort(target, offset);
    }

    private static sun.misc.Unsafe getUnsafe() {
        sun.misc.Unsafe unsafe = null;
        try {
            unsafe = AccessController.doPrivileged(new PrivilegedExceptionAction<sun.misc.Unsafe>() {
                @Override
                public sun.misc.Unsafe run() throws Exception {
                    Class<sun.misc.Unsafe> k = sun.misc.Unsafe.class;

                    for (Field f : k.getDeclaredFields()) {
                        f.setAccessible(true);
                        Object x = f.get(null);
                        if (k.isInstance(x)) {
                            return k.cast(x);
                        }
                    }
                    // The sun.misc.Unsafe field does not exist.
                    return null;
                }
            });
        } catch (Throwable ignored) {
        }
        return unsafe;
    }

    public static ByteOrder nativeByteOrder() {
        if (nativeByteOrder == null)
            throw new Error("Unknown byte order");
        return nativeByteOrder;
    }

    public static boolean bigOrder() {
        if (nativeByteOrder == null)
            throw new Error("Unknown byte order");
        return nativeByteOrder == ByteOrder.BIG_ENDIAN;
    }

    public static boolean littleOrder() {
        if (nativeByteOrder == null)
            throw new Error("Unknown byte order");
        return nativeByteOrder == ByteOrder.LITTLE_ENDIAN;
    }

    public static long objectFieldOffset(Field field) {
        return UNSAFE.objectFieldOffset(field);
    }

    public static void putBoolean(Object target, long offset, boolean value) {
        UNSAFE.putBoolean(target, offset, value);
    }

    public static void putByte(long address, byte value) {
        UNSAFE.putByte(address, value);
    }

    public static void putByte(Object target, long offset, byte value) {
        UNSAFE.putByte(target, offset, value);
    }

    public static void putDouble(Object target, long offset, double value) {
        UNSAFE.putDouble(target, offset, value);
    }

    public static void putFloat(Object target, long offset, float value) {
        UNSAFE.putFloat(target, offset, value);
    }

    public static void putInt(long address, int value) {
        UNSAFE.putInt(address, value);
    }

    public static void putInt(Object target, long offset, int value) {
        UNSAFE.putInt(target, offset, value);
    }

    public static void putLong(long address, long value) {
        UNSAFE.putLong(address, value);
    }

    public static void putLong(Object target, long offset, long value) {
        UNSAFE.putLong(target, offset, value);
    }

    public static void putObject(Object target, long offset, Object value) {
        UNSAFE.putObject(target, offset, value);
    }

    public static void putShort(long address, short value) {
        UNSAFE.putShort(address, value);
    }

    public static void putShort(Object target, long offset, short value) {
        UNSAFE.putShort(target, offset, value);
    }

    // These methods do no bounds checking.  Verification that the copy will not
    // result in memory corruption should be done prior to invocation.
    // All positions and lengths are specified in bytes.

    public static void setMemory(long address, long numBytes, byte value) {
        UNSAFE.setMemory(address, numBytes, value);
    }

    private static boolean supportsUnsafeArrayOperations() {
        boolean supported = false;
        if (UNSAFE != null) {
            try {
                Class<?> clazz = UNSAFE.getClass();
                clazz.getMethod("objectFieldOffset", Field.class);
                clazz.getMethod("allocateInstance", Class.class);
                clazz.getMethod("arrayBaseOffset", Class.class);
                clazz.getMethod("getByte", Object.class, long.class);
                clazz.getMethod("putByte", Object.class, long.class, byte.class);
                clazz.getMethod("getBoolean", Object.class, long.class);
                clazz.getMethod("putBoolean", Object.class, long.class, boolean.class);
                clazz.getMethod("getInt", Object.class, long.class);
                clazz.getMethod("putInt", Object.class, long.class, int.class);
                clazz.getMethod("getLong", Object.class, long.class);
                clazz.getMethod("putLong", Object.class, long.class, long.class);
                clazz.getMethod("getFloat", Object.class, long.class);
                clazz.getMethod("putFloat", Object.class, long.class, float.class);
                clazz.getMethod("getDouble", Object.class, long.class);
                clazz.getMethod("putDouble", Object.class, long.class, double.class);
                clazz.getMethod("getObject", Object.class, long.class);
                clazz.getMethod("putObject", Object.class, long.class, Object.class);
                clazz.getMethod("copyMemory", Object.class, long.class, Object.class, long.class, long.class);
                supported = true;
            } catch (Throwable e) {
                // Do nothing.
            }
        }
        return supported;
    }

    private static boolean supportsUnsafeByteBufferOperations() {
        boolean supported = false;
        if (UNSAFE != null) {
            try {
                Class<?> clazz = UNSAFE.getClass();
                // Methods for getting direct buffer address.
                clazz.getMethod("objectFieldOffset", Field.class);
                clazz.getMethod("getLong", Object.class, long.class);

                clazz.getMethod("getByte", long.class);
                clazz.getMethod("putByte", long.class, byte.class);
                clazz.getMethod("getInt", long.class);
                clazz.getMethod("putInt", long.class, int.class);
                clazz.getMethod("getLong", long.class);
                clazz.getMethod("putLong", long.class, long.class);
                clazz.getMethod("setMemory", long.class, long.class, byte.class);
                clazz.getMethod("copyMemory", long.class, long.class, long.class);
                supported = true;
            } catch (Throwable e) {
                // Do nothing.
            }
        }
        return supported;
    }

}
