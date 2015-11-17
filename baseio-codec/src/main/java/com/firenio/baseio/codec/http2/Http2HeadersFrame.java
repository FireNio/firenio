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
package com.firenio.baseio.codec.http2;

public class Http2HeadersFrame extends Http2Frame {

    private boolean e;
    private boolean endStream;
    private byte    padLength;
    private int     streamDependency;
    private short   weight;

    @Override
    public Http2FrameType getHttp2FrameType() {
        return Http2FrameType.FRAME_TYPE_HEADERS;
    }

    public byte getPadLength() {
        return padLength;
    }

    public int getStreamDependency() {
        return streamDependency;
    }

    public short getWeight() {
        return weight;
    }

    public boolean isE() {
        return e;
    }

    @Override
    public boolean isSilent() {
        return !endStream;
    }

    public void setE(boolean e) {
        this.e = e;
    }

    public void setEndStream(boolean endStream) {
        this.endStream = endStream;
    }

    public void setPadLength(byte padLength) {
        this.padLength = padLength;
    }

    public void setStreamDependency(int streamDependency) {
        this.streamDependency = streamDependency;
    }

    public void setWeight(short weight) {
        this.weight = weight;
    }

}
