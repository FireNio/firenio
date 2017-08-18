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

public interface Http2HeaderTable {

    void maxHeaderTableSize(long max) throws Http2Exception;

    /**
     * Represents the value for <a
     * href="https://tools.ietf.org/html/rfc7540#section-6.5.2"
     * >SETTINGS_HEADER_TABLE_SIZE</a>. The initial value returned by this
     * method must be {@link Http2CodecUtil#DEFAULT_HEADER_TABLE_SIZE}.
     */
    long maxHeaderTableSize();

    void maxHeaderListSize(long max) throws Http2Exception;

    /**
     * Represents the value for <a
     * href="https://tools.ietf.org/html/rfc7540#section-6.5.2"
     * >SETTINGS_MAX_HEADER_LIST_SIZE</a>. The initial value returned by this
     * method must be {@link Http2CodecUtil#DEFAULT_HEADER_LIST_SIZE}.
     */
    long maxHeaderListSize();
}
