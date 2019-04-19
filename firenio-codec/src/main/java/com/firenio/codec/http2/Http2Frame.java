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
package com.firenio.codec.http2;

import com.firenio.component.Frame;

//ERROR CODE
//
//Name         Code        Description
//NO_ERROR     0x0     Graceful shutdown
//PROTOCOL_ERROR       0x1     Protocol error detected
//INTERNAL_ERROR       0x2     Implementation fault
//FLOW_CONTROL_ERROR   0x3     Flow-control limits exceeded
//SETTINGS_TIMEOUT 0x4     Settings not acknowledged
//STREAM_CLOSED        0x5     Frame received for closed stream
//FRAME_SIZE_ERROR 0x6     Frame size incorrect
//REFUSED_STREAM       0x7     Stream not processed
//CANCEL           0x8     Stream cancelled
//COMPRESSION_ERROR    0x9     Compression state not updated
//CONNECT_ERROR        0xa     TCP connection error for CONNECT method
//ENHANCE_YOUR_CALM    0xb     Processing capacity exceeded
//INADEQUATE_SECURITY  0xc     Negotiated TLS parameters not acceptable
//HTTP_1_1_REQUIRED    0xd     Use HTTP/1.1 for the request
public abstract class Http2Frame extends Frame {

    private byte flags;
    private int  streamIdentifier;

    public byte getFlags() {
        return flags;
    }

    public abstract Http2FrameType getHttp2FrameType();

    public int getStreamIdentifier() {
        return streamIdentifier;
    }

    public void setFlags(byte flags) {
        this.flags = flags;
    }

    public void setStreamIdentifier(int streamIdentifier) {
        this.streamIdentifier = streamIdentifier;
    }

}
