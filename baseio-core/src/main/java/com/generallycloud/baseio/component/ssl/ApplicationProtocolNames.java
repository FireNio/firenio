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
package com.generallycloud.baseio.component.ssl;

public final class ApplicationProtocolNames {

    /**
     * {@code "h2"}: HTTP version 2
     */
    public static final String HTTP_2   = "h2";

    /**
     * {@code "http/1.1"}: HTTP version 1.1
     */
    public static final String HTTP_1_1 = "http/1.1";

    /**
     * {@code "spdy/3.1"}: SPDY version 3.1
     */
    public static final String SPDY_3_1 = "spdy/3.1";

    /**
     * {@code "spdy/3"}: SPDY version 3
     */
    public static final String SPDY_3   = "spdy/3";

    /**
     * {@code "spdy/2"}: SPDY version 2
     */
    public static final String SPDY_2   = "spdy/2";

    /**
     * {@code "spdy/1"}: SPDY version 1
     */
    public static final String SPDY_1   = "spdy/1";

    private ApplicationProtocolNames() {}
}
