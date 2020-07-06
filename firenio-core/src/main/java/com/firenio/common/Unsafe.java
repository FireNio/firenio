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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;

import com.firenio.Options;


/**
 * @author wangkai
 */
@SuppressWarnings("restriction")
public class Unsafe {

    public static final  int             BUF_UNSAFE            = 0;
    public static final  int             BUF_DIRECT            = 1;
    public static final  int             BUF_HEAP              = 2;
    public static final  long            ARRAY_BASE_OFFSET;
    public static final  long            BUFFER_ADDRESS_OFFSET;
    public static final  boolean         IS_LINUX              = isLinux();
    public static final  boolean         IS_ANDROID            = isAndroid();
    public static final  boolean         UNSAFE_AVAILABLE      = isUnsafeAvailable();
    public static final  boolean         UNSAFE_BUF_AVAILABLE;
    public static final  boolean         DIRECT_BUFFER_AVAILABLE;
    // This number limits the number of bytes to copy per call to Unsafe's
    // copyMemory method. A writeIndex is imposed to allow for safe point polling
    // during a large copy
    private static final long            UNSAFE_COPY_THRESHOLD = 1024L * 1024L;
    private static final boolean         BIG_ORDER             = checkBigOrder();
    private static final boolean         RAW_DIRECT_AVAILABLE;
    private static final int             JAVA_VERSION          = majorJavaVersion();
    private static final Constructor<?>  DIRECT_BUFFER_CONSTRUCTOR;
    private static final int             CAS_UPDATE_YIELD_TIME = 16;
    private static final sun.misc.Unsafe U;

    //jdk.internal.misc.Unsafe
    //jdk.internal.ref.Cleaner

    static {
        if (UNSAFE_AVAILABLE) {
            U = getUnsafe();
            UNSAFE_BUF_AVAILABLE = Options.isEnableUnsafeBuf();
            BUFFER_ADDRESS_OFFSET = fieldOffset(Util.getDeclaredField(Buffer.class, "address"));
            DIRECT_BUFFER_AVAILABLE = BUFFER_ADDRESS_OFFSET != -1;
            ARRAY_BASE_OFFSET = U.arrayBaseOffset(byte[].class);
            DIRECT_BUFFER_CONSTRUCTOR = getDirectBufferConstructor();
            RAW_DIRECT_AVAILABLE = DIRECT_BUFFER_CONSTRUCTOR != null;
        } else {
            if (Options.isEnableUnsafeBuf()) {
                throw new Error("UnsafeBuf enabled but no unsafe available");
            }
            U = null;
            UNSAFE_BUF_AVAILABLE = false;
            BUFFER_ADDRESS_OFFSET = -1;
            DIRECT_BUFFER_AVAILABLE = false;
            ARRAY_BASE_OFFSET = -1;
            DIRECT_BUFFER_CONSTRUCTOR = null;
            RAW_DIRECT_AVAILABLE = false;
        }
    }

    private Unsafe() {}

    private static Constructor<?> getDirectBufferConstructor() {
        Constructor<?>   directBufferConstructor = null;
        final ByteBuffer direct                  = ByteBuffer.allocateDirect(1);
        long             address                 = -1;
        try {
            final Object res = AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    try {
                        final Constructor<?> constructor = direct.getClass().getDeclaredConstructor(long.class, int.class);
                        Throwable            cause       = Util.trySetAccessible(constructor);
                        if (cause != null) {
                            return cause;
                        }
                        return constructor;
                    } catch (Throwable e) {
                        return e;
                    }
                }
            });
            if (res instanceof Constructor<?>) {
                address = allocate(1);
                try {
                    ((Constructor<?>) res).newInstance(address, 1);
                    directBufferConstructor = (Constructor<?>) res;
                } catch (Throwable e) {
                    return null;
                }
            } else {
                return null;
            }
        } finally {
            if (address != -1) {
                free(address);
            }
        }
        return directBufferConstructor;
    }

    private static void checkUnsafeAvailable() {
        if (!UNSAFE_AVAILABLE) {
            throw new RuntimeException("Unsafe not available");
        }
    }

    private static boolean isAndroid() {
        return "Dalvik".equalsIgnoreCase(System.getProperty("java.vm.name"));
    }

    private static boolean isLinux() {
        return Util.getStringProperty("os.name", "").toLowerCase().startsWith("lin");
    }

    private static boolean isUnsafeAvailable() {
        return !IS_ANDROID && Options.isEnableUnsafe();
    }

    public static int addAndGetInt(Object object, int offset, int add) {
        int expect = getIntVolatile(object, offset);
        int update = expect + add;
        if (compareAndSwapInt(object, offset, expect, update)) {
            return update;
        }
        for (int i = 0; i < CAS_UPDATE_YIELD_TIME; i++) {
            expect = getIntVolatile(object, offset);
            update = expect + add;
            if (compareAndSwapInt(object, offset, expect, update)) {
                return update;
            }
        }
        for (; ; ) {
            expect = getIntVolatile(object, offset);
            update = expect + add;
            if (compareAndSwapInt(object, offset, expect, update)) {
                return update;
            } else {
                Thread.yield();
            }
        }
    }

    public static long addAndGetLong(Object object, long offset, long add) {
        long expect = getLongVolatile(object, offset);
        long update = expect + add;
        if (compareAndSwapLong(object, offset, expect, update)) {
            return update;
        }
        for (int i = 0; i < CAS_UPDATE_YIELD_TIME; i++) {
            expect = getLongVolatile(object, offset);
            update = expect + add;
            if (compareAndSwapLong(object, offset, expect, update)) {
                return update;
            }
        }
        for (; ; ) {
            expect = getLongVolatile(object, offset);
            update = expect + add;
            if (compareAndSwapLong(object, offset, expect, update)) {
                return update;
            } else {
                Thread.yield();
            }
        }
    }

    public static int getMemoryTypeId() {
        if (UNSAFE_BUF_AVAILABLE) {
            return BUF_UNSAFE;
        } else if (DIRECT_BUFFER_AVAILABLE) {
            return BUF_DIRECT;
        } else {
            return BUF_HEAP;
        }
    }

    public static String getMemoryType() {
        if (UNSAFE_BUF_AVAILABLE) {
            return "unsafe";
        } else if (DIRECT_BUFFER_AVAILABLE) {
            return "direct";
        } else {
            return "heap";
        }
    }

    public static long address(ByteBuffer buffer) {
        checkUnsafeAvailable();
        return U.getLong(buffer, BUFFER_ADDRESS_OFFSET);
    }

    public static long allocate(long length) {
        checkUnsafeAvailable();
        return U.allocateMemory(length);
    }

    public static Object allocateInstance(Class<?> clazz) {
        checkUnsafeAvailable();
        try {
            return U.allocateInstance(clazz);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean compareAndSwapInt(Object o, long offset, int expect, int val) {
        checkUnsafeAvailable();
        return U.compareAndSwapInt(o, offset, expect, val);
    }

    public static boolean compareAndSwapLong(Object o, long offset, long expect, long val) {
        checkUnsafeAvailable();
        return U.compareAndSwapLong(o, offset, expect, val);
    }

    public static boolean compareAndSwapObject(Object o, long offset, Object expect, Object val) {
        checkUnsafeAvailable();
        return U.compareAndSwapObject(o, offset, expect, val);
    }

    public static void copyFromArray(byte[] src, long srcPos, long dstAddr, long length) {
        checkUnsafeAvailable();
        long offset = ARRAY_BASE_OFFSET + srcPos;
        U.copyMemory(src, offset, null, dstAddr, length);
    }

    public static void copyMemory(long srcAddress, long targetAddress, long length) {
        copyMemory(null, srcAddress, null, targetAddress, length);
    }

    public static void copyMemory(Object src, long srcOffset, Object target, long targetOffset, long length) {
        checkUnsafeAvailable();
        // see java.nio.Bits.copyToArray
        if (length < UNSAFE_COPY_THRESHOLD) {
            U.copyMemory(src, srcOffset, target, targetOffset, length);
        } else {
            for (; length > 0; ) {
                long size = Math.min(length, UNSAFE_COPY_THRESHOLD);
                U.copyMemory(src, srcOffset, target, targetOffset, size);
                length -= size;
                srcOffset += size;
                targetOffset += size;
            }
        }
    }

    public static void copyToArray(long srcAddr, byte[] dst, long dstPos, long length) {
        checkUnsafeAvailable();
        long dstOffset = ARRAY_BASE_OFFSET + dstPos;
        U.copyMemory(null, srcAddr, dst, dstOffset, length);
    }

    public static void free(long address) {
        checkUnsafeAvailable();
        U.freeMemory(address);
    }

    public static long getArrayBaseOffset() {
        return ARRAY_BASE_OFFSET;
    }

    public static boolean getBoolean(Object target, long offset) {
        checkUnsafeAvailable();
        return U.getBoolean(target, offset);
    }

    public static boolean getBooleanVolatile(Object target, long offset) {
        checkUnsafeAvailable();
        return U.getBooleanVolatile(target, offset);
    }

    public static byte getByte(long address) {
        checkUnsafeAvailable();
        return U.getByte(address);
    }

    public static byte getByte(Object target, long offset) {
        checkUnsafeAvailable();
        return U.getByte(target, offset);
    }

    public static double getDouble(Object target, long offset) {
        checkUnsafeAvailable();
        return U.getDouble(target, offset);
    }

    public static float getFloat(Object target, long offset) {
        checkUnsafeAvailable();
        return U.getFloat(target, offset);
    }

    public static int getInt(long address) {
        checkUnsafeAvailable();
        return U.getInt(address);
    }

    public static int getInt(Object target, long offset) {
        checkUnsafeAvailable();
        return U.getInt(target, offset);
    }

    public static int getIntVolatile(Object target, long offset) {
        checkUnsafeAvailable();
        return U.getIntVolatile(target, offset);
    }

    public static long getLong(long address) {
        checkUnsafeAvailable();
        return U.getLong(address);
    }

    public static long getLong(Object target, long offset) {
        checkUnsafeAvailable();
        return U.getLong(target, offset);
    }

    public static long getLongVolatile(Object target, long offset) {
        checkUnsafeAvailable();
        return U.getLongVolatile(target, offset);
    }

    public static Object getObject(Object target, long offset) {
        checkUnsafeAvailable();
        return U.getObject(target, offset);
    }

    public static Object getObjectVolatile(Object target, long offset) {
        checkUnsafeAvailable();
        return U.getObjectVolatile(target, offset);
    }

    public static short getShort(long address) {
        checkUnsafeAvailable();
        return U.getShort(address);
    }

    public static short getShort(Object target, long offset) {
        checkUnsafeAvailable();
        return U.getShort(target, offset);
    }

    private static boolean checkBigOrder() {
        return ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
    }

    public static boolean isBigOrder() {
        return BIG_ORDER;
    }

    public static boolean isLittleOrder() {
        return !BIG_ORDER;
    }

    public static long fieldOffset(Field field) {
        return field == null || U == null ? -1 : U.objectFieldOffset(field);
    }

    public static long fieldOffset(Class clazz, String fieldName) {
        Field field = Util.getDeclaredField(clazz, fieldName);
        if (field == null) {
            throw new RuntimeException("no such field: " + fieldName);
        }
        return fieldOffset(field);
    }

    public static void putBoolean(Object target, long offset, boolean value) {
        checkUnsafeAvailable();
        U.putBoolean(target, offset, value);
    }

    public static void putBooleanVolatile(Object target, long offset, boolean value) {
        checkUnsafeAvailable();
        U.putBooleanVolatile(target, offset, value);
    }

    public static void putByte(long address, byte value) {
        checkUnsafeAvailable();
        U.putByte(address, value);
    }

    public static void putByte(Object target, long offset, byte value) {
        checkUnsafeAvailable();
        U.putByte(target, offset, value);
    }

    public static void putDouble(Object target, long offset, double value) {
        checkUnsafeAvailable();
        U.putDouble(target, offset, value);
    }

    public static void putFloat(Object target, long offset, float value) {
        checkUnsafeAvailable();
        U.putFloat(target, offset, value);
    }

    public static void putInt(long address, int value) {
        checkUnsafeAvailable();
        U.putInt(address, value);
    }

    public static void putInt(Object target, long offset, int value) {
        checkUnsafeAvailable();
        U.putInt(target, offset, value);
    }

    public static void putIntVolatile(Object target, long offset, int value) {
        checkUnsafeAvailable();
        U.putIntVolatile(target, offset, value);
    }

    public static void putLong(long address, long value) {
        checkUnsafeAvailable();
        U.putLong(address, value);
    }

    public static void putLong(Object target, long offset, long value) {
        checkUnsafeAvailable();
        U.putLong(target, offset, value);
    }

    public static void putLongVolatile(Object target, long offset, long value) {
        checkUnsafeAvailable();
        U.putLongVolatile(target, offset, value);
    }

    public static void putObject(Object target, long offset, Object value) {
        checkUnsafeAvailable();
        U.putObject(target, offset, value);
    }

    public static void putObjectVolatile(Object target, long offset, Object value) {
        checkUnsafeAvailable();
        U.putObjectVolatile(target, offset, value);
    }

    public static void putShort(long address, short value) {
        checkUnsafeAvailable();
        U.putShort(address, value);
    }

    public static void putShort(Object target, long offset, short value) {
        checkUnsafeAvailable();
        U.putShort(target, offset, value);
    }

    public static void setMemory(long address, long numBytes, byte value) {
        checkUnsafeAvailable();
        U.setMemory(address, numBytes, value);
    }

    public static int javaVersion() {
        return JAVA_VERSION;
    }

    public static ByteBuffer allocateDirectByteBuffer(int cap) {
        if (RAW_DIRECT_AVAILABLE) {
            long address = allocate(cap);
            if (address == -1) {
                throw new RuntimeException("no enough space(direct): " + cap);
            }
            try {
                return (ByteBuffer) DIRECT_BUFFER_CONSTRUCTOR.newInstance(address, cap);
            } catch (Throwable e) {
                free(address);
                throw new Error(e);
            }
        } else {
            return ByteBuffer.allocateDirect(cap);
        }
    }

    public static void freeByteBuffer(ByteBuffer buffer) {
        if (buffer.isDirect()) {
            if (RAW_DIRECT_AVAILABLE) {
                free(address(buffer));
            } else {
                if (((sun.nio.ch.DirectBuffer) buffer).cleaner() != null) {
                    ((sun.nio.ch.DirectBuffer) buffer).cleaner().clean();
                } else {
                    free(address(buffer));
                }
            }
        }
    }

    private static int majorJavaVersion() {
        final String   javaSpecVersion = Util.getStringProperty("java.specification.version", "1.6");
        final String[] components      = javaSpecVersion.split("\\.");
        final int[]    version         = new int[components.length];
        for (int i = 0; i < components.length; i++) {
            version[i] = Integer.parseInt(components[i]);
        }
        if (version[0] == 1) {
            assert version[1] >= 6;
            return version[1];
        } else {
            return version[0];
        }
    }

    public static int arrayBaseOffset(Class<?> clazz) {
        checkUnsafeAvailable();
        return U.arrayBaseOffset(clazz);
    }

    public static int arrayIndexScale(Class<?> clazz) {
        checkUnsafeAvailable();
        return U.arrayIndexScale(clazz);
    }

    private static sun.misc.Unsafe getUnsafe() {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<sun.misc.Unsafe>() {
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
                    throw new Error("unsafe is null");
                }
            });
        } catch (Throwable e) {
            throw new Error("get unsafe failed", e);
        }
    }

    public static void putOrderedObject(Object target, long offset, Object value) {
        checkUnsafeAvailable();
        U.putOrderedObject(target, offset, value);
    }

    public static Object getAndSetObject(Object target, long offset, Object value) {
        checkUnsafeAvailable();
        return U.getAndSetObject(target, offset, value);
    }
}
