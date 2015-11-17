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

public enum Http2FrameType {

    FRAME_TYPE_CONTINUATION(0x09), FRAME_TYPE_DATA(0x00), FRAME_TYPE_GOAWAY(
            0x07), FRAME_TYPE_HEADERS(0x01), FRAME_TYPE_PING(0x06), FRAME_TYPE_PRIORITY(
                    0x02), FRAME_TYPE_PUSH_PROMISE(0x05), FRAME_TYPE_RST_STREAM(
                            0x03), FRAME_TYPE_SETTINGS(0x04), FRAME_TYPE_WINDOW_UPDATE(0x08);

    private static Http2FrameType[] VALUES;

    static {
        VALUES = new Http2FrameType[values().length];
        Http2FrameType[] values = Http2FrameType.values();
        for (Http2FrameType v : values) {
            VALUES[v.value] = v;
        }
    }

    private byte byteValue;

    private int  value;

    private Http2FrameType(int value) {
        this.value = value;
        this.byteValue = (byte) value;
    }

    public byte getByteValue() {
        return byteValue;
    }

    public int getValue() {
        return value;
    }

    public static Http2FrameType getValue(int i) {
        return VALUES[i];
    }

}
