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
public enum HttpVersion {

    HTTP1_0("HTTP/1.0"), HTTP1_1("HTTP/1.1"), HTTP_UNKNOW("HTTP/UNKNOW");

    private String value;

    private HttpVersion(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static HttpVersion getVersion(String version) {
        if (HTTP1_1.value.equals(version)) {
            return HTTP1_1;
        } else if (HTTP1_0.value.equals(version)) {
            return HTTP1_0;
        } else {
            return HttpVersion.HTTP_UNKNOW;
        }
    }

}
