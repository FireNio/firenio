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

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

public class StringUtil {

    private static final Field   stringValueField;
    private static final boolean enableGetStringValueField;

    static {
        Object res = AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                try {
                    Field value = String.class.getDeclaredField("value");
                    Throwable cause = ClassUtil.trySetAccessible(value);
                    if (cause != null) {
                        return cause;
                    }
                    return value;
                } catch (Throwable e) {
                    return e;
                }
            }
        });
        enableGetStringValueField = !(res instanceof Throwable);
        if (enableGetStringValueField) {
            stringValueField = (Field) res;
        } else {
            stringValueField = null;
        }
    }

    public static boolean isNullOrBlank(String value) {
        return value == null || value.length() == 0;
    }

    public static boolean hasLength(String text) {
        return text != null && text.length() > 0;
    }

    public static boolean hasText(String text) {
        return text != null && text.trim().length() > 0;
    }

    //FIXME 尽量使用decode取代new String()
    //see  java.lang.StringCoding.decode(Charset cs, byte[] ba, int off, int len)
    public static String decode(Charset charset, ByteBuffer buffer) {
        return charset.decode(buffer).toString();
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

    public static List<String> toList(String... protocols) {
        return toList(16, protocols);
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

    public static char[] stringToCharArray(String str) {
        if (!enableGetStringValueField) {
            return str.toCharArray();
        }
        try {
            return (char[]) stringValueField.get(str);
        } catch (Exception e) {
            return str.toCharArray();
        }
    }

    public static boolean isTrueValue(String value) {
        return "true".equals(value) || "1".equals(value);
    }

    public static int indexOf(StringBuilder sb, char ch) {
        int count = sb.length();
        for (int i = 0; i < count; i++) {
            if (ch == sb.charAt(i)) {
                return i;
            }
        }
        return -1;
    }

    public static int lastIndexOf(StringBuilder sb, char ch) {
        int count = sb.length();
        for (int i = count - 1; i > -1; i--) {
            if (ch == sb.charAt(i)) {
                return i;
            }
        }
        return -1;
    }

}
