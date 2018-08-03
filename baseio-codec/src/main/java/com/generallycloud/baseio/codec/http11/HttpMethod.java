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

/**
 * @author wangkai
 *
 */
public enum HttpMethod {

    GET("GET"), POST("POST"), OTHER("OTHER");

    private final String value;

    private final byte[] bytes;

    private HttpMethod(String value) {
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
        } else {
            return HttpMethod.OTHER;
        }
    }

    public byte[] getBytes() {
        return bytes;
    }

}
