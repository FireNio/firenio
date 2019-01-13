/*
 * Copyright 2015 The Baseio Project
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
package com.firenio.baseio.common;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.PrintStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.firenio.baseio.LifeCycle;
import com.firenio.baseio.Releasable;
import com.firenio.baseio.component.ChannelAcceptor;
import com.firenio.baseio.log.Logger;
import com.firenio.baseio.log.LoggerFactory;

/**
 * @author wangkai
 *
 */
public class Util {

    public static final Charset ASCII  = Charset.forName("ASCII");
    public static final Charset GBK    = Charset.forName("GBK");
    public static final Charset UTF8   = Charset.forName("UTF-8");
    private static final Logger logger = LoggerFactory.getLogger(Util.class);

    @SuppressWarnings("rawtypes")
    public static List array2List(Object[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        List<Object> list = new ArrayList<>(array.length);
        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }
        return list;

    }

    public static int availableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

    public static void close(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public static void close(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public static void close(Selector selector) {
        if (selector == null) {
            return;
        }
        try {
            selector.close();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public static void exec(Runnable runnable) {
        exec(runnable, null);
    }

    public static void exec(Runnable runnable, String name) {
        if (!isNullOrBlank(name)) {
            new Thread(runnable, name).start();
        } else {
            new Thread(runnable).start();
        }
    }

    public static Class<?> forName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static Class<?> forName(String className, Class<?> defaultClass) {
        if (isNullOrBlank(className)) {
            return defaultClass;
        }
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return defaultClass;
        }
    }

    public static boolean getBooleanProperty(String key) {
        return getBooleanProperty(key, false);
    }

    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String v = System.getProperty(key);
        if (!isNullOrBlank(v)) {
            try {
                return isTrueValue(v);
            } catch (Exception e) {}
        }
        return defaultValue;
    }

    public static Field getDeclaredField(Class<?> clazz, String name) {
        if (clazz == null) {
            return null;
        }
        Field[] fs = clazz.getDeclaredFields();
        for (Field f : fs) {
            if (f.getName().equals(name)) {
                return f;
            }
        }
        return null;
    }

    public static Field getDeclaredFieldFC(Class<?> clazz, String name) {
        Class<?> c = clazz;
        for (;;) {
            if (c == null) {
                return null;
            }
            Field f = getDeclaredField(c, name);
            if (f == null) {
                c = c.getSuperclass();
                continue;
            }
            return f;
        }
    }

    public static Class<?>[] getInterfaces(Class<?> clazz) {
        if (!clazz.isInterface()) {
            return clazz.getInterfaces();
        }
        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces.length == 0) {
            return new Class[] { clazz };
        }
        List<Class<?>> cs = new ArrayList<>(interfaces.length + 1);
        for (Class<?> c : interfaces) {
            cs.add(c);
        }
        cs.add(clazz);
        return cs.toArray(new Class[cs.size()]);
    }

    public static int getIntProperty(String key) {
        return getIntProperty(key, 0);
    }

    public static int getIntProperty(String key, int defaultValue) {
        String v = System.getProperty(key);
        if (!isNullOrBlank(v)) {
            try {
                return Integer.parseInt(v);
            } catch (Exception e) {}
        }
        return defaultValue;
    }

    public static String getStringProperty(String key) {
        return getStringProperty(key, null);
    }

    public static String getStringProperty(String key, String defaultValue) {
        String v = System.getProperty(key);
        if (!isNullOrBlank(v)) {
            return v;
        }
        return defaultValue;
    }

    public static String getValueFromArray(String[] args, int index) {
        return getValueFromArray(args, index, null);
    }

    public static String getValueFromArray(String[] args, int index, String defaultValue) {
        if (index < 0 || args == null) {
            return defaultValue;
        }
        if (index >= args.length) {
            return defaultValue;
        }
        return args[index];
    }

    public static Object getValueOfLast(Object target, String fieldName) {
        try {
            Object c = target;
            for (;;) {
                Field fieldNext = getDeclaredFieldFC(c.getClass(), fieldName);
                if (fieldNext == null) {
                    return c;
                }
                trySetAccessible(fieldNext);
                Object next = fieldNext.get(c);
                if (next == null) {
                    return c;
                }
                c = next;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean hasLength(String text) {
        return text != null && text.length() > 0;
    }

    public static boolean hasText(String text) {
        return text != null && text.trim().length() > 0;
    }

    public static int indexOf(CharSequence sb, char ch) {
        return indexOf(sb, ch, 0);
    }

    public static int indexOf(CharSequence sb, char ch, int index) {
        int count = sb.length();
        for (int i = index; i < count; i++) {
            if (ch == sb.charAt(i)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.size() == 0;
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.size() == 0;
    }

    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isNullOrBlank(String value) {
        return value == null || value.length() == 0;
    }

    public static boolean isTrueValue(String value) {
        return "true".equals(value) || "1".equals(value);
    }

    public static int lastIndexOf(CharSequence sb, char ch) {
        int count = sb.length();
        for (int i = count - 1; i > -1; i--) {
            if (ch == sb.charAt(i)) {
                return i;
            }
        }
        return -1;
    }

    public static int lastIndexOf(String str, char ch, int length) {
        int end = -1;
        if (str.length() > length) {
            end = str.length() - length - 1;
        }
        for (int i = str.length() - 1; i > end; i--) {
            if (str.charAt(i) == ch) {
                return i;
            }
        }
        return -1;
    }

    public static Object newInstance(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static long now() {
        return System.currentTimeMillis();
    }

    public static long past(long start) {
        return now() - start;
    }

    public static void printArray(int[] array) {
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i]);
            System.out.print(",");
        }
        System.out.println();
    }

    public static void printArray(Object[] array) {
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i]);
            System.out.print(",");
        }
        System.out.println();
    }

    public static String randomLeastSignificantBits() {
        UUID uuid = UUID.randomUUID();
        byte[] array = new byte[8];
        ByteUtil.putLong(array, uuid.getLeastSignificantBits(), 0);
        return ByteUtil.getHexString(array);
    }

    public static String randomMostSignificantBits() {
        UUID uuid = UUID.randomUUID();
        byte[] array = new byte[8];
        ByteUtil.putLong(array, uuid.getMostSignificantBits(), 0);
        return ByteUtil.getHexString(array);
    }

    public static String randomUUID() {
        UUID uuid = UUID.randomUUID();
        byte[] array = new byte[16];
        ByteUtil.putLong(array, uuid.getMostSignificantBits(), 0);
        ByteUtil.putLong(array, uuid.getLeastSignificantBits(), 8);
        return ByteUtil.getHexString(array);
    }

    public static void release(Object releasable) {
        if (releasable instanceof Releasable) {
            try {
                ((Releasable) releasable).release();
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public static void setObjectValue(Object target, Object value, String fieldName) {
        try {
            Field field = getDeclaredFieldFC(target.getClass(), fieldName);
            if (field == null) {
                throw new NoSuchFieldException(fieldName);
            }
            trySetAccessible(field);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setSystemPropertiesIfNull(String key, String value) {
        if (System.getProperty(key) == null) {
            System.setProperty(key, value);
        }
    }

    public static void setValueOfLast(Object target, Object value, String fieldName) {
        Object last = getValueOfLast(target, fieldName);
        setObjectValue(last, value, fieldName);
    }

    public static int skip(StringBuilder sb, char ch) {
        return skip(sb, ch, 0);
    }

    public static int skip(StringBuilder sb, char ch, int index) {
        int count = sb.length();
        for (int i = index; i < count; i++) {
            if (ch != sb.charAt(i)) {
                return i;
            }
        }
        return -1;
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {}
    }

    public static String stackTraceToString(Throwable cause) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream pout = new PrintStream(out);
        cause.printStackTrace(pout);
        pout.flush();
        try {
            return new String(out.toByteArray());
        } finally {
            Util.close(out);
        }
    }

    public static void start(LifeCycle lifeCycle) throws Exception {
        if (lifeCycle != null && !lifeCycle.isRunning()) {
            lifeCycle.start();
        }
    }

    public static void stop(LifeCycle lifeCycle) {
        if (lifeCycle != null) {
            try {
                lifeCycle.stop();
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public static void testUuid() {

        int count = 1024 * 1024;
        long start = System.currentTimeMillis();
        String str = null;
        for (int i = 0; i < count; i++) {
            str = randomUUID();
            //            str = UUID.randomUUID().toString();
        }
        System.out.println(str);
        System.out.println(System.currentTimeMillis() - start);

    }

    public static List<String> toList(int initialListSize, String... protocols) {
        if (protocols == null) {
            return null;
        }
        List<String> result = new ArrayList<>(initialListSize);
        for (String p : protocols) {
            if (p == null || p.isEmpty()) {
                throw new IllegalArgumentException("protocol cannot be null or empty");
            }
            result.add(p);
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("protocols cannot empty");
        }
        return result;
    }

    public static List<String> toList(String... protocols) {
        return toList(16, protocols);
    }

    public static Throwable trySetAccessible(AccessibleObject object) {
        try {
            object.setAccessible(true);
            return null;
        } catch (Exception e) {
            return e;
        }
    }

    public static void unbind(ChannelAcceptor unbindable) {
        if (unbindable == null) {
            return;
        }
        try {
            unbindable.unbind();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public static <T extends Throwable> T unknownStackTrace(T cause, Class<?> clazz,
            String method) {
        cause.setStackTrace(new StackTraceElement[] {
                new StackTraceElement(clazz.getName(), method, null, -1) });
        return cause;
    }

    public static int valueOf(int value, byte[] data) {
        int v = value;
        for (int i = data.length - 1; i > -1; i--) {
            data[i] = ByteUtil.getNumCharByte(v % 10);
            v = v / 10;
            if (v == 0) {
                return i;
            }
        }
        return -1;
    }

    public static void wait(Object o) {
        synchronized (o) {
            try {
                o.wait();
            } catch (InterruptedException e) {}
        }
    }

    public static void wait(Object o, long timeout) {
        synchronized (o) {
            try {
                o.wait(timeout);
            } catch (InterruptedException e) {}
        }
    }

    public static int clothCover(int v) {
        int n = 2;
        for (; n < v;)
            n <<= 1;
        return n;
    }
    
    private static final char   DELIM_START = '{';
    private static final String DELIM_STR   = "{}";
    private static final char   ESCAPE_CHAR = '\\';

    final public static String arrayFormat(final String messagePattern, final Object[] argArray) {
        if (messagePattern == null) {
            return null;
        }
        if (argArray == null) {
            return messagePattern;
        }
        int i = 0;
        int j;
        StringBuilder builder = new StringBuilder(messagePattern.length() + 50);
        int L;
        for (L = 0; L < argArray.length; L++) {
            j = messagePattern.indexOf(DELIM_STR, i);
            if (j == -1) {
                // no more variables
                if (i == 0) { // this is a simple string
                    return messagePattern;
                } else { // add the tail string which contains no variables
                             // and return
                         // the result.
                    builder.append(messagePattern.substring(i, messagePattern.length()));
                    return builder.toString();
                }
            } else {
                if (isEscapedDelimeter(messagePattern, j)) {
                    if (!isDoubleEscaped(messagePattern, j)) {
                        L--; // DELIM_START was escaped, thus should not
                             // be incremented
                        builder.append(messagePattern.substring(i, j - 1));
                        builder.append(DELIM_START);
                        i = j + 1;
                    } else {
                        // The escape character preceding the delimiter
                        // start is
                        // itself escaped: "abc x:\\{}"
                        // we have to consume one backward slash
                        builder.append(messagePattern.substring(i, j - 1));
                        deeplyAppendParameter(builder, argArray[L], new HashMap<>());
                        i = j + 2;
                    }
                } else {
                    // normal case
                    builder.append(messagePattern.substring(i, j));
                    deeplyAppendParameter(builder, argArray[L], new HashMap<>());
                    i = j + 2;
                }
            }
        }
        // append the characters following the last {} pair.
        builder.append(messagePattern.substring(i, messagePattern.length()));
        if (L < argArray.length - 1) {
            return builder.toString();
        } else {
            return builder.toString();
        }
    }

    private static void booleanArrayAppend(StringBuilder sbuf, boolean[] a) {
        sbuf.append('[');
        final int len = a.length;
        for (int i = 0; i < len; i++) {
            sbuf.append(a[i]);
            if (i != len - 1) {
                sbuf.append(", ");
            }
        }
        sbuf.append(']');
    }

    private static void byteArrayAppend(StringBuilder sbuf, byte[] a) {
        sbuf.append('[');
        final int len = a.length;
        for (int i = 0; i < len; i++) {
            sbuf.append(a[i]);
            if (i != len - 1) {
                sbuf.append(", ");
            }
        }
        sbuf.append(']');
    }

    private static void charArrayAppend(StringBuilder sbuf, char[] a) {
        sbuf.append('[');
        final int len = a.length;
        for (int i = 0; i < len; i++) {
            sbuf.append(a[i]);
            if (i != len - 1) {
                sbuf.append(", ");
            }
        }
        sbuf.append(']');
    }

    // special treatment of array values was suggested by 'lizongbo'
    private static void deeplyAppendParameter(StringBuilder sbuf, Object o, Map<?, ?> seenMap) {
        if (o == null) {
            sbuf.append("null");
            return;
        }
        if (!o.getClass().isArray()) {
            safeObjectAppend(sbuf, o);
        } else {
            // check for primitive array types because they
            // unfortunately cannot be cast to Object[]
            if (o instanceof boolean[]) {
                booleanArrayAppend(sbuf, (boolean[]) o);
            } else if (o instanceof byte[]) {
                byteArrayAppend(sbuf, (byte[]) o);
            } else if (o instanceof char[]) {
                charArrayAppend(sbuf, (char[]) o);
            } else if (o instanceof short[]) {
                shortArrayAppend(sbuf, (short[]) o);
            } else if (o instanceof int[]) {
                intArrayAppend(sbuf, (int[]) o);
            } else if (o instanceof long[]) {
                longArrayAppend(sbuf, (long[]) o);
            } else if (o instanceof float[]) {
                floatArrayAppend(sbuf, (float[]) o);
            } else if (o instanceof double[]) {
                doubleArrayAppend(sbuf, (double[]) o);
            } else {
                objectArrayAppend(sbuf, (Object[]) o, seenMap);
            }
        }
    }

    private static void doubleArrayAppend(StringBuilder sbuf, double[] a) {
        sbuf.append('[');
        final int len = a.length;
        for (int i = 0; i < len; i++) {
            sbuf.append(a[i]);
            if (i != len - 1) {
                sbuf.append(", ");
            }
        }
        sbuf.append(']');
    }

    private static void floatArrayAppend(StringBuilder sbuf, float[] a) {
        sbuf.append('[');
        final int len = a.length;
        for (int i = 0; i < len; i++) {
            sbuf.append(a[i]);
            if (i != len - 1) {
                sbuf.append(", ");
            }
        }
        sbuf.append(']');
    }

    final public static String format(String messagePattern, Object arg) {
        return arrayFormat(messagePattern, new Object[] { arg });
    }

    final public static String format(final String messagePattern, Object arg1, Object arg2) {
        return arrayFormat(messagePattern, new Object[] { arg1, arg2 });
    }

    private static void intArrayAppend(StringBuilder sbuf, int[] a) {
        sbuf.append('[');
        final int len = a.length;
        for (int i = 0; i < len; i++) {
            sbuf.append(a[i]);
            if (i != len - 1) {
                sbuf.append(", ");
            }
        }
        sbuf.append(']');
    }

    private final static boolean isDoubleEscaped(String messagePattern, int delimeterStartIndex) {
        if (delimeterStartIndex >= 2
                && messagePattern.charAt(delimeterStartIndex - 2) == ESCAPE_CHAR) {
            return true;
        } else {
            return false;
        }
    }

    private final static boolean isEscapedDelimeter(String messagePattern,
            int delimeterStartIndex) {
        if (delimeterStartIndex == 0) {
            return false;
        }
        char potentialEscape = messagePattern.charAt(delimeterStartIndex - 1);
        if (potentialEscape == ESCAPE_CHAR) {
            return true;
        } else {
            return false;
        }
    }

    private static void longArrayAppend(StringBuilder sbuf, long[] a) {
        sbuf.append('[');
        final int len = a.length;
        for (int i = 0; i < len; i++) {
            sbuf.append(a[i]);
            if (i != len - 1) {
                sbuf.append(", ");
            }
        }
        sbuf.append(']');
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void objectArrayAppend(StringBuilder sbuf, Object[] a, Map seenMap) {
        sbuf.append('[');
        if (!seenMap.containsKey(a)) {
            seenMap.put(a, null);
            final int len = a.length;
            for (int i = 0; i < len; i++) {
                deeplyAppendParameter(sbuf, a[i], seenMap);
                if (i != len - 1) {
                    sbuf.append(", ");
                }
            }
            // allow repeats in siblings
            seenMap.remove(a);
        } else {
            sbuf.append("...");
        }
        sbuf.append(']');
    }

    private static void safeObjectAppend(StringBuilder sbuf, Object o) {
        try {
            String oAsString = o.toString();
            sbuf.append(oAsString);
        } catch (Throwable t) {
            System.err.println("SLF4J: Failed toString() invocation on an object of type ["
                    + o.getClass().getName() + "]");
            t.printStackTrace();
            sbuf.append("[FAILED toString()]");
        }
    }

    private static void shortArrayAppend(StringBuilder sbuf, short[] a) {
        sbuf.append('[');
        final int len = a.length;
        for (int i = 0; i < len; i++) {
            sbuf.append(a[i]);
            if (i != len - 1) {
                sbuf.append(", ");
            }
        }
        sbuf.append(']');
    }

}
