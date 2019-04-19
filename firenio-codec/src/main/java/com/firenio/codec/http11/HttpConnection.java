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
package com.firenio.codec.http11;

import com.firenio.common.Util;

/**
 * @author wangkai
 */
public enum HttpConnection {

    NONE(0, ""), CLOSE(1, "close"), KEEP_ALIVE(2, "keep-alive"), UPGRADE(3, "Upgrade");

    private static final HttpConnection[] enums;

    static {
        enums = new HttpConnection[values().length];
        for (HttpConnection m : values()) {
            enums[m.id] = m;
        }
    }

    private final int id;

    private final String value;

    private final byte[] line;

    HttpConnection(int id, String value) {
        this.id = id;
        this.value = value;
        if (Util.isNullOrBlank(value)) {
            this.line = null;
        } else {
            this.line = ("\r\nConnection: " + value).getBytes();
        }
    }

    public static HttpConnection get(int index) {
        return enums[index];
    }

    public byte[] getLine() {
        return line;
    }

    public int getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

}
