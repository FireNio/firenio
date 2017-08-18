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
package com.generallycloud.baseio.protocol;

import com.generallycloud.baseio.buffer.ByteBuf;

public interface SslFuture extends ChannelFuture {

    public static final int SSL_CONTENT_TYPE_ALERT              = 21;

    public static final int SSL_CONTENT_TYPE_APPLICATION_DATA   = 23;

    public static final int SSL_CONTENT_TYPE_CHANGE_CIPHER_SPEC = 20;

    public static final int SSL_CONTENT_TYPE_HANDSHAKE          = 22;

    public static final int SSL_RECORD_HEADER_LENGTH            = 5;

    public ByteBuf getProduce();

}
