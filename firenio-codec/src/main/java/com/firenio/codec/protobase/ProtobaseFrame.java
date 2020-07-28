/*
 * Copyright 2015 The FireNio Project
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
package com.firenio.codec.protobase;

import com.firenio.component.Frame;

import static com.firenio.codec.protobase.ProtobaseCodec.*;

public class ProtobaseFrame extends Frame {

    private byte flags = (byte) (make_type(TYPE_TEXT) | LAST);
    private long channelId;
    private long frameId;

    public void setLast() {
        this.flags |= LAST;
    }

    @Override
    public boolean isLast() {
        return (flags & LAST) != 0;
    }

    public void setText() {
        setType(TYPE_TEXT);
    }

    @Override
    public boolean isText() {
        return getType() == TYPE_TEXT;
    }

    public void setBinary() {
        this.setType(TYPE_BINARY);
    }

    public boolean isBinary() {
        return getType() == TYPE_BINARY;
    }

    public void setBroadcast() {
        this.flags |= BROADCAST;
    }

    public boolean isBroadcast() {
        return (flags & BROADCAST) != 0;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setFrameId(long frameId) {
        this.frameId = frameId;
    }

    public long getFrameId() {
        return frameId;
    }

    public void setFlags(byte flags) {
        this.flags = flags;
    }

    public byte getFlags() {
        return flags;
    }

    public void setType(byte type) {
        this.flags = (byte) ((flags & 0b1111) | make_type(type));
    }

    public int getType() {
        return get_type(flags);
    }

    public void setContinue() {
        this.flags &= ~LAST;
    }

    public void setTunnel() {
        this.flags &= ~BROADCAST;
    }


}
