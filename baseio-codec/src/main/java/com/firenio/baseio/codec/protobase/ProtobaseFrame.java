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

    private boolean broadcast;
    private int     channelId;
    private byte    extType;
    private int     frameId;
    private boolean last = true;
    private boolean text = true;

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

    public byte getExtType() {
        return extType;
    }

    public int getFrameId() {
        return frameId;
    }

    public byte[] getReadBinary() {
        if (isBinary()) {
            return getArrayContent();
        }
        return null;
    }

    @Override
    public int headerLength() {
        return ProtobaseCodec.PROTOCOL_HEADER;
    }

    @Override
    public boolean isBinary() {
        return !text;
    }

    public boolean isBroadcast() {
        return broadcast;
    }

    @Override
    public boolean isContinue() {
        return !last;
    }

    @Override
    public boolean isLast() {
        return last;
    }

    @Override
    public boolean isText() {
        return text;
    }

    public void setBroadcast(boolean broadcast) {
        this.broadcast = broadcast;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public void setExtType(byte extType) {
        this.extType = extType;
    }

    public void setFrameId(int frameId) {
        this.frameId = frameId;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public void setText(boolean text) {
        this.text = text;
    }

}
