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
package com.firenio.codec.http2;

public class Http2Header {

    private final int index;

    private final String name;

    private final int    size;
    private final String value;

    public Http2Header(int index, String name, String value) {
        this.index = index;
        this.name = name;
        this.value = value;
        this.size = sizeOf(name, value);
    }

    public Http2Header(String name, String value) {
        this(0, name, value);
    }

    public static int sizeOf(String name, String value) {
        return name.length() + value.length();
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public int size() {
        return size;
    }
}
