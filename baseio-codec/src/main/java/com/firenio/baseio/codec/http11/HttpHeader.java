/*
 * Copyright 2015 The Baseio Project
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"),
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

import java.util.HashMap;
import java.util.Map;

public enum HttpHeader {

    Accept("Accept"), //
    Accept_Encoding("Accept-Encoding"), //
    Accept_Language("Accept-Language"), //
    Accept_Ranges("Accept-Ranges"), //
    Age("Age"), // 
    Allow("Allow"), // 
    Cache_Control("Cache-Control"), // 
    Connection("Connection"), // 
    Content_Encoding("Content-Encoding"), // 
    Content_Language("Content-Language"), // 
    Content_Length("Content-Length"), // 
    Content_Location("Content-Location"), // 
    Content_MD5("Content-MD5"), // 
    Content_Range("Content-Range"), // 
    Content_Type("Content-Type"), // 
    Cookie("Cookie"), // 
    Date("Date"), // 
    ETag("ETag"), // 
    Expires("Expires"), // 
    Host("Host"), // 
    If_Modified_Since("If-Modified-Since"), // 
    Last_Modified("Last-Modified"), // 
    Location("Location"), // 
    Pragma("Pragma"), // 
    Proxy_Authenticate("Proxy-Authenticate"), // 
    Proxy_Connection("Proxy-Connection"), // 
    Referer("Referer"), // 
    Refresh("Refresh"), // 
    Retry_After("Retry-After"), // 
    Sec_Metadata("Sec-Metadata"), // 
    Sec_WebSocket_Accept("Sec-WebSocket-Accept"), // 
    Sec_WebSocket_Extensions("Sec-WebSocket-Extensions"), // 
    Sec_WebSocket_Key("Sec-WebSocket-Key"), // 
    Sec_WebSocket_Version("Sec-WebSocket-Version"), // 
    Server("Server"), // 
    Set_Cookie("Set-Cookie"), // 
    Trailer("Trailer"), // 
    Transfer_Encoding("Transfer-Encoding"), // 
    Upgrade("Upgrade"), // 
    Upgrade_Insecure_Requests("Upgrade-Insecure-Requests"), // 
    User_Agent("User-Agent"), // 
    Vary("Vary"), // 
    Via("Via"), // 
    Warning("Warning"), // 
    WWW_Authenticate("WWW-Authenticate");

    public static final Map<String, HttpHeader> ALL = new HashMap<>();
    private static final HttpHeader[]           enums;
    static {
        try {
            enums = new HttpHeader[values().length];
            for (HttpHeader value : values()) {
                enums[value.id] = value;
                ALL.put(value.getKey(), value);
                ALL.put(value.getLowercase(), value);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private final byte[] bytes;

    private final int    id;

    private final String key;

    private final String lowercase;

    private HttpHeader(String key) {
        this.key = key;
        this.lowercase = key.toLowerCase();
        this.bytes = key.getBytes();
        this.id = HttpHeaderHelper.HEADER_SEQ.getAndIncrement();
    }

    public byte[] getBytes() {
        return bytes;
    }

    public int getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getLowercase() {
        return lowercase;
    }

    @Override
    public String toString() {
        return key.toString();
    }

    public static HttpHeader get(int index) {
        return enums[index];
    }

}
