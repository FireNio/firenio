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

import com.generallycloud.baseio.protocol.NamedFuture;
import com.generallycloud.baseio.protocol.TextFuture;

public interface ProtobaseFuture extends NamedFuture, TextFuture {

    int getFutureId();

    byte[] getReadBinary();

    int getReadBinarySize();

    int getChannelId();

    int getChannelKey();

    int getTextLength();

    byte[] getWriteBinary();

    int getWriteBinarySize();

    boolean hasReadBinary();

    boolean isBroadcast();

    void setBroadcast(boolean broadcast);

    void setFutureId(int futureId);

    void setFutureName(String futureName);

    void setChannelId(int channelId);

    void writeBinary(byte b);

    void writeBinary(byte[] bytes);

    void writeBinary(byte[] bytes, int offset, int length);

}
