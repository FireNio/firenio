/*
 * Copyright 2015 The Baseio Project
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
package com.generallycloud.baseio.codec.http2;

import com.generallycloud.baseio.protocol.AbstractFrame;

public abstract class Http2FrameHeader extends AbstractFrame implements Http2Frame {

    private byte            flags;
    private int             streamIdentifier;

    @Override
    public byte getFlags() {
        return flags;
    }

    @Override
    public int getStreamIdentifier() {
        return streamIdentifier;
    }

    @Override
    public void setFlags(byte flags) {
        this.flags = flags;
    }

    @Override
    public void setStreamIdentifier(int streamIdentifier) {
        this.streamIdentifier = streamIdentifier;
    }

}
