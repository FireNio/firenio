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
package com.generallycloud.baseio.codec.http2.hpack;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class Http2HeadersImpl implements Http2Headers {

    private HashMap<String, String> headers = new HashMap<>();

    private String                  method;
    private String                  scheme;
    private String                  authority;
    private String                  path;
    private String                  status;

    @Override
    public Iterator<Entry<String, String>> iterator() {
        return headers.entrySet().iterator();
    }

    @Override
    public Http2Headers method(String value) {
        this.method = value;
        this.headers.put(":method", value);
        return this;
    }

    @Override
    public Http2Headers scheme(String value) {
        this.scheme = value;
        this.headers.put(":scheme", value);
        return this;
    }

    @Override
    public Http2Headers authority(String value) {
        this.authority = value;
        this.headers.put(":authority", value);
        return this;
    }

    @Override
    public Http2Headers path(String value) {
        this.path = value;
        this.headers.put(":path", value);
        return this;
    }

    @Override
    public Http2Headers status(String value) {
        this.status = value;
        this.headers.put(":status", value);
        return this;
    }

    @Override
    public String method() {
        return method;
    }

    @Override
    public String scheme() {
        return scheme;
    }

    @Override
    public String authority() {
        return authority;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public String status() {
        return status;
    }

    @Override
    public void add(String name, String value) {
        this.headers.put(name, value);
    }

}
