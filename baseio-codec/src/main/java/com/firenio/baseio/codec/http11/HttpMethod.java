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
package com.firenio.baseio.codec.http11;

/**
 * @author wangkai
 *
 */
public enum HttpMethod {

    CONNECT(3, "CONNECT"), GET(1, "GET"), OTHER(0, "OTHER"), POST(2, "POST");

    private static final HttpMethod[] enums;

    static {
        enums = new HttpMethod[values().length];
        for (HttpMethod m : values()) {
            enums[m.id] = m;
        }
    }

    private final byte[] bytes;

    private final int    id;

    private final String value;

    private HttpMethod(int id, String value) {
        this.id = id;
        this.value = value;
        this.bytes = value.getBytes();
    }

    public byte[] getBytes() {
        return bytes;
    }

    public int getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public static HttpMethod get(int index) {
        return enums[index];
    }

}
