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

import java.util.Collection;
import java.util.Map;

public abstract class Assert {

    public static void expectFalse(boolean expression) {
        expectFalse(expression, "expression must be false");
    }

    public static void expectFalse(boolean expression, String message) {
        if (expression) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void expectTrue(boolean expression) {
        expectTrue(expression, "expression must be true");
    }

    public static void expectTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void hasText(String text) {
        hasText(text, "argument must have text; it must not be null, empty, or blank");
    }

    public static void hasText(String text, String message) {
        if (!Util.hasText(text)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(Collection<?> collection) {
        notEmpty(collection, "empty collection");
    }

    public static void notEmpty(Collection<?> collection, String message) {
        if (Util.isEmpty(collection)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(Map<?, ?> map) {
        notEmpty(map, "empty map");
    }

    public static void notEmpty(Map<?, ?> map, String message) {
        if (Util.isEmpty(map)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(Object[] array) {
        notEmpty(array, "empty array");
    }

    public static void notEmpty(Object[] array, String message) {
        if (Util.isEmpty(array)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notNull(Object object) {
        notNull(object, "argument is required; it must not be null");
    }

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

}
