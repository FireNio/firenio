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
public enum HttpMethod {

    GET(1, "GET"), POST(2, "POST"), CONNECT(3, "CONNECT"), OTHER(0, "OTHER");

    private final String value;

    private final int    id;

    private final byte[] bytes;

    private HttpMethod(int id, String value) {
        this.id = id;
        this.value = value;
        this.bytes = value.getBytes();
    }

    public String getValue() {
        return value;
    }

    public static HttpMethod getMethod(String method) {
        if (GET.value.equals(method)) {
            return GET;
        } else if (POST.value.equals(method)) {
            return POST;
        } else if (CONNECT.value.equals(method)) {
            return CONNECT;
        } else {
            return HttpMethod.OTHER;
        }
    }

    public int getId() {
        return id;
    }

    public byte[] getBytes() {
        return bytes;
    }

    private static final HttpMethod[] enums;

    static {
        enums = new HttpMethod[values().length];
        for (HttpMethod m : values()) {
            enums[m.id] = m;
        }
    }

    public static HttpMethod get(int index) {
        return enums[index];
    }

}
