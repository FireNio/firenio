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
package com.generallycloud.baseio.codec.http11;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public final class HttpHeader {

    public static final HttpHeader              Accept                    = C("Accept");
    public static final HttpHeader              Accept_Encoding           = C("Accept-Encoding");
    public static final HttpHeader              Accept_Language           = C("Accept-Language");
    public static final HttpHeader              Accept_Ranges             = C("Accept-Ranges");
    public static final HttpHeader              Age                       = C("Age");
    public static final Map<String, HttpHeader> ALL                       = new HashMap<>();
    public static final HttpHeader              Allow                     = C("Allow");
    public static final HttpHeader              Cache_Control             = C("Cache-Control");
    public static final HttpHeader              Connection                = C("Connection");
    public static final HttpHeader              Content_Encoding          = C("Content-Encoding");
    public static final HttpHeader              Content_Language          = C("Content-Language");
    public static final HttpHeader              Content_Length            = C("Content-Length");
    public static final HttpHeader              Content_Location          = C("Content-Location");
    public static final HttpHeader              Content_MD5               = C("Content-MD5");
    public static final HttpHeader              Content_Range             = C("Content-Range");
    public static final HttpHeader              Content_Type              = C("Content-Type");
    public static final HttpHeader              Cookie                    = C("Cookie");
    public static final HttpHeader              Date                      = C("Date");
    public static final HttpHeader              ETag                      = C("ETag");
    public static final HttpHeader              Expires                   = C("Expires");
    public static final HttpHeader              Host                      = C("Host");
    public static final HttpHeader              Last_Modified             = C("Last-Modified");
    public static final HttpHeader              Location                  = C("Location");
    public static final HttpHeader              Pragma                    = C("Pragma");
    public static final HttpHeader              Proxy_Authenticate        = C("Proxy-Authenticate");
    public static final HttpHeader              Proxy_Connection          = C("Proxy-Connection");
    public static final HttpHeader              Referer                   = C("Referer");
    public static final HttpHeader              Refresh                   = C("Refresh");
    public static final HttpHeader              Retry_After               = C("Retry-After");
    public static final HttpHeader              Sec_Metadata              = C("Sec-Metadata");
    public static final HttpHeader              Sec_WebSocket_Accept      = C(
            "Sec-WebSocket-Accept");
    public static final HttpHeader              Sec_WebSocket_Key         = C("Sec-WebSocket-Key");
    public static final HttpHeader              Server                    = C("Server");
    public static final HttpHeader              Set_Cookie                = C("Set-Cookie");
    public static final HttpHeader              Trailer                   = C("Trailer");
    public static final HttpHeader              Transfer_Encoding         = C("Transfer-Encoding");
    public static final HttpHeader              Upgrade                   = C("Upgrade");
    public static final HttpHeader              Upgrade_Insecure_Requests = C(
            "Upgrade-Insecure-Requests");
    public static final HttpHeader              User_Agent                = C("User-Agent");
    public static final HttpHeader              Vary                      = C("Vary");
    public static final HttpHeader              Via                       = C("Via");
    public static final HttpHeader              Warning                   = C("Warning");
    public static final HttpHeader              WWW_Authenticate          = C("WWW-Authenticate");
    public static final HttpHeader              If_Modified_Since         = C("If-Modified-Since");

    private final byte[]                        bytes;
    private final String                        key;
    private final String                        lowercase;

    static HttpHeader C(String key) {
        return new HttpHeader(key);
    }

    public HttpHeader(String key) {
        this.key = key;
        this.lowercase = key.toLowerCase();
        this.bytes = key.getBytes();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HttpHeader) {
            return key.equals(((HttpHeader) obj).key);
        }
        return false;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getKey() {
        return key;
    }

    public String getLowercase() {
        return lowercase;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
    
    @Override
    public String toString() {
        return key.toString();
    }
    
    static{
        initMappings();
    }

    static void initMappings() {
        try {
            Field[] fs = HttpHeader.class.getDeclaredFields();
            for (Field f : fs) {
                if (f.getType() == HttpHeader.class) {
                    HttpHeader value = (HttpHeader) f.get(null);
                    ALL.put(value.getKey(), value);
                    ALL.put(value.getLowercase(), value);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
