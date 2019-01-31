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
package com.firenio.baseio.codec.protobase;

import com.firenio.baseio.component.Frame;

public class ProtobaseFrame extends Frame {

    static final byte MASK_BINARY;
    static final byte MASK_BROADCAST;
    static final byte MASK_CONTINUE;
    static final byte MASK_EXT_TYPE;
    static final byte MASK_EXT_TYPE_INVERSE;
    static final byte MASK_LAST;
    static final byte MASK_TEXT;
    static final byte MASK_TUNNEL;

    static {
        MASK_TEXT = 0b0010_0000;
        MASK_TUNNEL = 0b0100_0000;
        MASK_LAST = 0b0001_0000;
        MASK_EXT_TYPE = 0b0000_1111;
        MASK_BINARY = (byte) ~MASK_TEXT;
        MASK_BROADCAST = (byte) ~MASK_TUNNEL;
        MASK_CONTINUE = (byte) ~MASK_LAST;
        MASK_EXT_TYPE_INVERSE = (byte) ~MASK_EXT_TYPE;
    }

    private int  channelId;
    private byte flags = (byte) (MASK_TEXT | MASK_TUNNEL | MASK_LAST);
    private int  frameId;

    public ProtobaseFrame() {}

    public ProtobaseFrame(int frameId) {
        this.frameId = frameId;
    }

    public int getChannelId() {
        return channelId;
    }

    public int getChannelKey() {
        return channelId;
    }

    public int getExtType() {
        return flags & MASK_EXT_TYPE;
    }

    public int getFrameId() {
        return frameId;
    }

    public byte[] getReadBinary() {
        if (isText()) {
            return null;
        }
        return getArrayContent();
    }

    @Override
    public int headerLength() {
        return ProtobaseCodec.PROTOCOL_HEADER;
    }

    @Override
    public boolean isLast() {
        return (flags & MASK_LAST) != 0;
    }

    @Override
    public boolean isText() {
        return (flags & MASK_TEXT) != 0;
    }

    //is tunnel or broadcast
    public boolean isTunnel(){
        return (flags & MASK_TUNNEL) != 0;
    }

    public void setBinary() {
        this.flags &= MASK_BINARY;
    }

    public void setBroadcast() {
        this.flags &= MASK_BROADCAST;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public void setContinue() {
        this.flags &= MASK_CONTINUE;
    }

    public void setExtType(byte extType) {
        this.flags &= MASK_EXT_TYPE_INVERSE;
        this.flags |= extType;
    }
    
    public void setFlags(byte flags) {
        this.flags = flags;
    }
    
    public byte getFlags() {
        return flags;
    }

    public void setFrameId(int frameId) {
        this.frameId = frameId;
    }

    public void setLast() {
        this.flags |= MASK_LAST;
    }
    
    public void setText() {
        this.flags |= MASK_TEXT;
    }

    public void setTunnel() {
        this.flags |= MASK_TUNNEL;
    }
    
}
