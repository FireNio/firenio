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


/**
 * @author wangkai
 */
@SuppressWarnings("restriction")
public class Unsafe {

    public static final  long           ARRAY_BASE_OFFSET;
    public static final  long           BUFFER_ADDRESS_OFFSET;
    // This number limits the number of bytes to copy per call to Unsafe's
    // copyMemory method. A limit is imposed to allow for safe point polling
    // during a large copy
    private static final long           UNSAFE_COPY_THRESHOLD = 1024L * 1024L;
    private static final ByteOrder      nativeByteOrder;
    private static final InternalUnsafe UNSAFE;
    private static final int            JAVA_VERSION          = majorJavaVersion();
    private static final Constructor<?> DIRECT_BUFFER_CONSTRUCTOR;

    //jdk.internal.misc.Unsafe
    //jdk.internal.ref.Cleaner

    static {
        UNSAFE = getUnsafe();
        BUFFER_ADDRESS_OFFSET = fieldOffset(Util.getDeclaredField(Buffer.class, "address"));
        ARRAY_BASE_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);
        nativeByteOrder = detectByteOrder();
        DIRECT_BUFFER_CONSTRUCTOR = getDirectBufferConstructor();
    }

    private Unsafe() {}

    private static Constructor<?> getDirectBufferConstructor() {
        Constructor<?> directBufferConstructor;
        final ByteBuffer     direct  = ByteBuffer.allocateDirect(1);
        long           address = -1;
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
                    } catch (Exception e) {
                        return e;
                    }
                }
            });
            if (res instanceof Constructor<?>) {
                address = allocate(1);
                try {
                    ((Constructor<?>) res).newInstance(address, 1);
                    directBufferConstructor = (Constructor<?>) res;
                } catch (Exception e) {
                    throw new Error("get DirectBuffer Constructor failed", e);
                }
            } else {
                throw new Error("get DirectBuffer Constructor failed", (Throwable) res);
            }
        } finally {
            if (address != -1) {
                free(address);
            }
            free(address(direct));
        }
        return directBufferConstructor;
    }


    private static InternalUnsafe getUnsafe() {
        //        if (javaVersion() > 8) {
        //            return new Jdk8UpUnsafe();
        //        } else {
        //            return new Jdk8AndLowUnsafe();
        //        }
        return new Jdk8AndLowUnsafe();
    }

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
     * @param src     source array
     * @param srcPos  offset within source array of the first element to native_read
     * @param dstAddr destination address
     * @param length  number of bytes to copy
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
     * @param srcAddr source address
     * @param dst     destination array
     * @param dstPos  offset within destination array of the first element to write
     * @param length  number of bytes to copy
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
                    throw new Error("detect byte order failed");
            }
        } finally {
            UNSAFE.freeMemory(a);
        }
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

    public static ByteOrder nativeByteOrder() {
        return nativeByteOrder;
    }

    public static boolean isBigOrder() {
        return nativeByteOrder() == ByteOrder.BIG_ENDIAN;
    }

    public static boolean isLittleOrder() {
        return nativeByteOrder() == ByteOrder.LITTLE_ENDIAN;
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

    public static void setMemory(long address, long numBytes, byte value) {
        UNSAFE.setMemory(address, numBytes, value);
    }

    public static int javaVersion() {
        return JAVA_VERSION;
    }

    public static ByteBuffer allocateDirectByteBuffer(int cap) {
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
    }

    public static void freeByteBuffer(ByteBuffer buffer) {
        if (buffer.isDirect()) {
            //            boolean free = false;
            //            if (javaVersion() > 8) {
            //                sun.nio.ch.DirectBuffer  b = (sun.nio.ch.DirectBuffer) buffer;
            //                jdk.internal.ref.Cleaner c = b.cleaner();
            //                if (c != null) {
            //                    c.clean();
            //                    free = true;
            //                }
            //            } else {
            //                if (((sun.nio.ch.DirectBuffer) buffer).cleaner() != null) {
            //                    ((sun.nio.ch.DirectBuffer) buffer).cleaner().clean();
            //                    free = true;
            //                }
            //            }
            //            if (!free) {
            //                free(address(buffer));
            //            }
            free(address(buffer));
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

    interface InternalUnsafe {

        boolean compareAndSwapInt(Object o, long offset, int expected, int val);

        boolean compareAndSwapLong(Object o, long offset, int expected, long val);

        boolean compareAndSwapObject(Object o, long offset, Object expected, Object val);

        void copyMemory(ByteBuffer buf, long targetAddress, long length);

        void copyMemory(long srcAddress, long targetAddress, long length);

        void copyMemory(Object src, long srcOffset, Object target, long targetOffset, long length);

        long fieldOffset(Field field);

        void free(long address);

        boolean getBoolean(Object target, long offset);

        byte getByte(long address);

        byte getByte(Object target, long offset);

        double getDouble(Object target, long offset);

        float getFloat(Object target, long offset);

        int getInt(long address);

        int getInt(Object target, long offset);

        long getLong(long address);

        long getLong(Object target, long offset);

        Object getObject(Object target, long offset);

        short getShort(long address);

        short getShort(Object target, long offset);

        long objectFieldOffset(Field field);

        void putBoolean(Object target, long offset, boolean value);

        void putByte(long address, byte value);

        void putByte(Object target, long offset, byte value);

        void putDouble(Object target, long offset, double value);

        void putFloat(Object target, long offset, float value);

        void putInt(long address, int value);

        void putInt(Object target, long offset, int value);

        void putLong(long address, long value);

        void putLong(Object target, long offset, long value);

        void putObject(Object target, long offset, Object value);

        void putShort(long address, short value);

        void putShort(Object target, long offset, short value);

        void setMemory(long address, long numBytes, byte value);

        long arrayBaseOffset(Class<?> clazz);

        long allocateMemory(long length);

        Object allocateInstance(Class<?> clazz) throws InstantiationException;

        void freeMemory(long address);
    }

    //    static class Jdk8UpUnsafe implements InternalUnsafe {
    //
    //        private jdk.internal.misc.Unsafe UNSAFE;
    //
    //        Jdk8UpUnsafe() {
    //            UNSAFE = getUnsafe();
    //        }
    //
    //        public boolean compareAndSwapInt(Object o, long offset, int expected, int val) {
    //            return UNSAFE.compareAndSetInt(o, offset, expected, val);
    //        }
    //
    //        public boolean compareAndSwapLong(Object o, long offset, int expected, long val) {
    //            return UNSAFE.compareAndSetLong(o, offset, expected, val);
    //        }
    //
    //        public boolean compareAndSwapObject(Object o, long offset, Object expected, Object val) {
    //            return UNSAFE.compareAndSetObject(o, offset, expected, val);
    //        }
    //
    //        public void copyMemory(ByteBuffer buf, long targetAddress, long length) {
    //            UNSAFE.copyMemory(address(buf) + buf.position(), targetAddress, length);
    //        }
    //
    //        public void copyMemory(long srcAddress, long targetAddress, long length) {
    //            UNSAFE.copyMemory(srcAddress, targetAddress, length);
    //        }
    //
    //        public void copyMemory(Object src, long srcOffset, Object target, long targetOffset, long length) {
    //            UNSAFE.copyMemory(src, srcOffset, target, targetOffset, length);
    //        }
    //
    //        public long fieldOffset(Field field) {
    //            return UNSAFE.objectFieldOffset(field);
    //        }
    //
    //        public void free(long address) {
    //            UNSAFE.freeMemory(address);
    //        }
    //
    //        public boolean getBoolean(Object target, long offset) {
    //            return UNSAFE.getBoolean(target, offset);
    //        }
    //
    //        public byte getByte(long address) {
    //            return UNSAFE.getByte(address);
    //        }
    //
    //        public byte getByte(Object target, long offset) {
    //            return UNSAFE.getByte(target, offset);
    //        }
    //
    //        public double getDouble(Object target, long offset) {
    //            return UNSAFE.getDouble(target, offset);
    //        }
    //
    //        public float getFloat(Object target, long offset) {
    //            return UNSAFE.getFloat(target, offset);
    //        }
    //
    //        public int getInt(long address) {
    //            return UNSAFE.getInt(address);
    //        }
    //
    //        public int getInt(Object target, long offset) {
    //            return UNSAFE.getInt(target, offset);
    //        }
    //
    //        public long getLong(long address) {
    //            return UNSAFE.getLong(address);
    //        }
    //
    //        public long getLong(Object target, long offset) {
    //            return UNSAFE.getLong(target, offset);
    //        }
    //
    //        public Object getObject(Object target, long offset) {
    //            return UNSAFE.getObject(target, offset);
    //        }
    //
    //        public short getShort(long address) {
    //            return UNSAFE.getShort(address);
    //        }
    //
    //        public short getShort(Object target, long offset) {
    //            return UNSAFE.getShort(target, offset);
    //        }
    //
    //        public long objectFieldOffset(Field field) {
    //            return UNSAFE.objectFieldOffset(field);
    //        }
    //
    //        public void putBoolean(Object target, long offset, boolean value) {
    //            UNSAFE.putBoolean(target, offset, value);
    //        }
    //
    //        public void putByte(long address, byte value) {
    //            UNSAFE.putByte(address, value);
    //        }
    //
    //        public void putByte(Object target, long offset, byte value) {
    //            UNSAFE.putByte(target, offset, value);
    //        }
    //
    //        public void putDouble(Object target, long offset, double value) {
    //            UNSAFE.putDouble(target, offset, value);
    //        }
    //
    //        public void putFloat(Object target, long offset, float value) {
    //            UNSAFE.putFloat(target, offset, value);
    //        }
    //
    //        public void putInt(long address, int value) {
    //            UNSAFE.putInt(address, value);
    //        }
    //
    //        public void putInt(Object target, long offset, int value) {
    //            UNSAFE.putInt(target, offset, value);
    //        }
    //
    //        public void putLong(long address, long value) {
    //            UNSAFE.putLong(address, value);
    //        }
    //
    //        public void putLong(Object target, long offset, long value) {
    //            UNSAFE.putLong(target, offset, value);
    //        }
    //
    //        public void putObject(Object target, long offset, Object value) {
    //            UNSAFE.putObject(target, offset, value);
    //        }
    //
    //        public void putShort(long address, short value) {
    //            UNSAFE.putShort(address, value);
    //        }
    //
    //        public void putShort(Object target, long offset, short value) {
    //            UNSAFE.putShort(target, offset, value);
    //        }
    //
    //        public void setMemory(long address, long numBytes, byte value) {
    //            UNSAFE.setMemory(address, numBytes, value);
    //        }
    //
    //        @Override
    //        public long arrayBaseOffset(Class<?> clazz) {
    //            return UNSAFE.arrayBaseOffset(clazz);
    //        }
    //
    //        @Override
    //        public long allocateMemory(long length) {
    //            return UNSAFE.allocateMemory(length);
    //        }
    //
    //        @Override
    //        public Object allocateInstance(Class<?> clazz) throws InstantiationException {
    //            return UNSAFE.allocateInstance(clazz);
    //        }
    //
    //        @Override
    //        public void freeMemory(long address) {
    //            UNSAFE.freeMemory(address);
    //        }
    //
    //        private jdk.internal.misc.Unsafe getUnsafe() {
    //            jdk.internal.misc.Unsafe unsafe = null;
    //            try {
    //                unsafe = AccessController.doPrivileged(new PrivilegedExceptionAction<jdk.internal.misc.Unsafe>() {
    //                    @Override
    //                    public jdk.internal.misc.Unsafe run() throws Exception {
    //                        Class<jdk.internal.misc.Unsafe> k = jdk.internal.misc.Unsafe.class;
    //                        for (Field f : k.getDeclaredFields()) {
    //                            f.setAccessible(true);
    //                            Object x = f.get(null);
    //                            if (k.isInstance(x)) {
    //                                return k.cast(x);
    //                            }
    //                        }
    //                        // The sun.misc.Unsafe field does not exist.
    //                        return null;
    //                    }
    //                });
    //            } catch (Throwable e) {
    //                throw new Error("get unsafe failed", e);
    //            }
    //            return unsafe;
    //        }
    //
    //    }


    static class Jdk8AndLowUnsafe implements InternalUnsafe {

        private sun.misc.Unsafe UNSAFE;

        Jdk8AndLowUnsafe() {
            UNSAFE = getUnsafe();
        }

        public boolean compareAndSwapInt(Object o, long offset, int expected, int val) {
            return UNSAFE.compareAndSwapInt(o, offset, expected, val);
        }

        public boolean compareAndSwapLong(Object o, long offset, int expected, long val) {
            return UNSAFE.compareAndSwapLong(o, offset, expected, val);
        }

        public boolean compareAndSwapObject(Object o, long offset, Object expected, Object val) {
            return UNSAFE.compareAndSwapObject(o, offset, expected, val);
        }

        public void copyMemory(ByteBuffer buf, long targetAddress, long length) {
            UNSAFE.copyMemory(address(buf) + buf.position(), targetAddress, length);
        }

        public void copyMemory(long srcAddress, long targetAddress, long length) {
            UNSAFE.copyMemory(srcAddress, targetAddress, length);
        }

        public void copyMemory(Object src, long srcOffset, Object target, long targetOffset, long length) {
            UNSAFE.copyMemory(src, srcOffset, target, targetOffset, length);
        }

        public long fieldOffset(Field field) {
            return UNSAFE.objectFieldOffset(field);
        }

        public void free(long address) {
            UNSAFE.freeMemory(address);
        }

        public boolean getBoolean(Object target, long offset) {
            return UNSAFE.getBoolean(target, offset);
        }

        public byte getByte(long address) {
            return UNSAFE.getByte(address);
        }

        public byte getByte(Object target, long offset) {
            return UNSAFE.getByte(target, offset);
        }

        public double getDouble(Object target, long offset) {
            return UNSAFE.getDouble(target, offset);
        }

        public float getFloat(Object target, long offset) {
            return UNSAFE.getFloat(target, offset);
        }

        public int getInt(long address) {
            return UNSAFE.getInt(address);
        }

        public int getInt(Object target, long offset) {
            return UNSAFE.getInt(target, offset);
        }

        public long getLong(long address) {
            return UNSAFE.getLong(address);
        }

        public long getLong(Object target, long offset) {
            return UNSAFE.getLong(target, offset);
        }

        public Object getObject(Object target, long offset) {
            return UNSAFE.getObject(target, offset);
        }

        public short getShort(long address) {
            return UNSAFE.getShort(address);
        }

        public short getShort(Object target, long offset) {
            return UNSAFE.getShort(target, offset);
        }

        public long objectFieldOffset(Field field) {
            return UNSAFE.objectFieldOffset(field);
        }

        public void putBoolean(Object target, long offset, boolean value) {
            UNSAFE.putBoolean(target, offset, value);
        }

        public void putByte(long address, byte value) {
            UNSAFE.putByte(address, value);
        }

        public void putByte(Object target, long offset, byte value) {
            UNSAFE.putByte(target, offset, value);
        }

        public void putDouble(Object target, long offset, double value) {
            UNSAFE.putDouble(target, offset, value);
        }

        public void putFloat(Object target, long offset, float value) {
            UNSAFE.putFloat(target, offset, value);
        }

        public void putInt(long address, int value) {
            UNSAFE.putInt(address, value);
        }

        public void putInt(Object target, long offset, int value) {
            UNSAFE.putInt(target, offset, value);
        }

        public void putLong(long address, long value) {
            UNSAFE.putLong(address, value);
        }

        public void putLong(Object target, long offset, long value) {
            UNSAFE.putLong(target, offset, value);
        }

        public void putObject(Object target, long offset, Object value) {
            UNSAFE.putObject(target, offset, value);
        }

        public void putShort(long address, short value) {
            UNSAFE.putShort(address, value);
        }

        public void putShort(Object target, long offset, short value) {
            UNSAFE.putShort(target, offset, value);
        }

        public void setMemory(long address, long numBytes, byte value) {
            UNSAFE.setMemory(address, numBytes, value);
        }

        @Override
        public long arrayBaseOffset(Class<?> clazz) {
            return UNSAFE.arrayBaseOffset(clazz);
        }

        @Override
        public long allocateMemory(long length) {
            return UNSAFE.allocateMemory(length);
        }

        @Override
        public Object allocateInstance(Class<?> clazz) throws InstantiationException {
            return UNSAFE.allocateInstance(clazz);
        }

        @Override
        public void freeMemory(long address) {
            UNSAFE.freeMemory(address);
        }

        private sun.misc.Unsafe getUnsafe() {
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
            } catch (Throwable e) {
                throw new Error("get unsafe failed", e);
            }
            return unsafe;
        }

    }


}
