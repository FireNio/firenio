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

import com.firenio.baseio.common.Util;

/**
 * @author wangkai
 *
 */
public class HttpContentType {

    public static final HttpContentType    none;
    public static final HttpContentType    application_js_utf8;
    public static final HttpContentType    application_json;
    public static final HttpContentType    application_stream;
    public static final HttpContentType    application_urlencoded;
    public static final HttpContentType    image_gif;
    public static final HttpContentType    image_icon;
    public static final HttpContentType    image_jpeg;
    public static final HttpContentType    image_png;
    public static final HttpContentType    multipart;
    public static final HttpContentType    server_baseio;
    public static final HttpContentType    text_css_utf8;
    public static final HttpContentType    text_html_utf8;
    public static final HttpContentType    text_plain;
    public static final HttpContentType    text_plain_gbk;
    public static final HttpContentType    text_plain_utf8;
    public static final HttpContentType    upgrade;
    public static final HttpContentType    websocket;
    private static int                     z_index;
    private static final HttpContentType[] z_enums;

    static {
        z_index = 0;
        z_enums = new HttpContentType[512];
        none = add("");
        application_js_utf8 = add("application/x-javascript;charset=utf-8");
        application_json = add("application/json");
        application_stream = add("application/octet-stream");
        application_urlencoded = add("application/x-www-form-urlencoded");
        image_gif = add("image/gif");
        image_icon = add("image/x-icon");
        image_jpeg = add("image/jpeg");
        image_png = add("image/png");
        multipart = add("multipart/form-data");
        server_baseio = add("baseio");
        text_css_utf8 = add("text/css;charset=utf-8");
        text_html_utf8 = add("text/html;charset=utf-8");
        text_plain = add("text/plain");
        text_plain_gbk = add("text/plain;charset=gbk");
        text_plain_utf8 = add("text/plain;charset=utf-8");
        upgrade = add("Upgrade");
        websocket = add("WebSocket");
    }

    private final int    id;

    private final String value;

    private final byte[] line;

    private HttpContentType(int id, String value) {
        this.id = id;
        this.value = value;
        if (Util.isNullOrBlank(value)) {
            this.line = null;
        } else {
            this.line = ("\r\nContent-Type: " + value).getBytes();
        }
    }

    public synchronized static HttpContentType add(String value) {
        int id = z_index++;
        z_enums[id] = new HttpContentType(id, value);
        return z_enums[id];
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

    public static HttpContentType get(int index) {
        return z_enums[index];
    }

}
