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

import com.generallycloud.baseio.protocol.BinaryFrame;
import com.generallycloud.baseio.protocol.TextFrame;

public class ProtobaseFrame extends BinaryFrame implements TextFrame {

    private int     channelId;
    private int     frameId;
    private byte    frameType;
    private boolean isBroadcast;
    private byte[]  readBinary;
    private String  readText;

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

    public int getFrameId() {
        return frameId;
    }

    public byte getFrameType() {
        return frameType;
    }

    public byte[] getReadBinary() {
        return readBinary;
    }

    public int getReadBinarySize() {
        if (hasReadBinary()) {
            return readBinary.length;
        }
        return 0;
    }

    @Override
    public String getReadText() {
        return readText;
    }

    public boolean hasReadBinary() {
        return readBinary != null;
    }

    public boolean isBroadcast() {
        return isBroadcast;
    }

    public void setBroadcast(boolean broadcast) {
        this.isBroadcast = broadcast;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public void setFrameId(int frameId) {
        this.frameId = frameId;
    }

    public void setFrameType(byte frameType) {
        this.frameType = frameType;
    }

    public void setReadBinary(byte[] readBinary) {
        this.readBinary = readBinary;
    }

    public void setReadText(String readText) {
        this.readText = readText;
    }

    @Override
    public String toString() {
        return getReadText();
    }

}
