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
package com.generallycloud.baseio.codec.protobase;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.codec.protobase.future.HashedProtobaseFuture;
import com.generallycloud.baseio.codec.protobase.future.ProtobaseFuture;

public class SessionIdProtobaseProtocolEncoder extends ProtobaseProtocolEncoder {

    @Override
    protected void putHeaderExtend(ProtobaseFuture future, ByteBuf buf) {
        buf.putInt(((HashedProtobaseFuture) future).getSessionId());
    }

    @Override
    protected int getHeaderLengthNoBinary() {
        return SessionIdProtobaseProtocolDecoder.PROTOCOL_HEADER_NO_BINARY;
    }

    @Override
    protected int getHeaderLengthWithBinary() {
        return SessionIdProtobaseProtocolDecoder.PROTOCOL_HEADER_WITH_BINARY;
    }

}
