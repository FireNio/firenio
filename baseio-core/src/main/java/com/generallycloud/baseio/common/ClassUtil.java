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
package com.generallycloud.baseio.common;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ClassUtil {

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

    public static Class<?> forName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static Class<?> forName(String className, Class<?> defaultClass) {
        if (StringUtil.isNullOrBlank(className)) {
            return defaultClass;
        }
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return defaultClass;
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

    public static Throwable trySetAccessible(AccessibleObject object) {
        try {
            object.setAccessible(true);
            return null;
        } catch (Exception e) {
            return e;
        }
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

    public static void setObjectValue(Object target, Object value, String fieldName) {
        try {
            Field field = getDeclaredFieldFC(target.getClass(), fieldName);
            if (field == null) {
                throw new NoSuchFieldException(fieldName);
            }
            ClassUtil.trySetAccessible(field);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setValueOfLast(Object target, Object value, String fieldName) {
        Object last = getValueOfLast(target, fieldName);
        setObjectValue(last, value, fieldName);
    }

    public static Object getValueOfLast(Object target, String fieldName) {
        try {
            Object c = target;
            for (;;) {
                Field fieldNext = ClassUtil.getDeclaredFieldFC(c.getClass(), fieldName);
                if (fieldNext == null) {
                    return c;
                }
                ClassUtil.trySetAccessible(fieldNext);
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

}
