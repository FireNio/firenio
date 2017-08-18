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
package com.generallycloud.baseio.codec.http11.future;

import com.generallycloud.baseio.protocol.NamedFuture;

public interface WebSocketFuture extends NamedFuture {

    public static final int    OP_CONTINUATION_FRAME     = 0;
    public static final int    OP_TEXT_FRAME             = 1;
    public static final int    OP_BINARY_FRAME           = 2;
    public static final int    OP_CONNECTION_CLOSE_FRAME = 8;
    public static final int    OP_PING_FRAME             = 9;
    public static final int    OP_PONG_FRAME             = 10;

    public static final int    HEADER_LENGTH             = 2;

    public static final String SESSION_KEY_SERVICE_NAME  = "_SESSION_KEY_SERVICE_NAME";

    public abstract boolean isEof();

    public abstract int getType();

    public abstract int getLength();

    public abstract boolean isCloseFrame();

    public abstract byte[] getByteArray();

}
