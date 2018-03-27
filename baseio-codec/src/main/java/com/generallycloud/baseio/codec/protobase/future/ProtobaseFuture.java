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
package com.generallycloud.baseio.codec.protobase.future;

import com.generallycloud.baseio.balance.HashedBalanceFuture;
import com.generallycloud.baseio.balance.SessionIdBalanceFuture;
import com.generallycloud.baseio.component.ByteArrayBuffer;
import com.generallycloud.baseio.protocol.NamedFuture;

public interface ProtobaseFuture extends NamedFuture, SessionIdBalanceFuture, HashedBalanceFuture {

    byte[] getBinary();

    int getBinaryLength();

    int getFutureId();

    int getTextLength();

    ByteArrayBuffer getWriteBinaryBuffer();

    boolean hasBinary();

    void setFutureId(int futureId);

    void setFutureName(String futureName);

    void writeBinary(byte b);

    void writeBinary(byte[] bytes);

    void writeBinary(byte[] bytes, int offset, int length);
    
}
