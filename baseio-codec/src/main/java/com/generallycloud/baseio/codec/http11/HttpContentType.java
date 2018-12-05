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
package com.generallycloud.baseio.codec.http11;

/**
 * @author wangkai
 *
 */
public enum HttpContentType {

    URLENCODED(1, "URLENCODED"), MULTIPART(2, "MULTIPART"), JSON(3, "JSON"), OTHER(0, "OTHER");

    private final String value;

    private final int    id;

    private final byte[] bytes;

    private HttpContentType(int id, String value) {
        this.id = id;
        this.value = value;
        this.bytes = value.getBytes();
    }

    public String getValue() {
        return value;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public int getId() {
        return id;
    }

    private static final HttpContentType[] enums;

    static {
        enums = new HttpContentType[values().length];
        for (HttpContentType m : values()) {
            enums[m.id] = m;
        }
    }

    public static HttpContentType get(int id) {
        return enums[id];
    }

}
