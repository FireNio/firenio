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
package com.generallycloud.baseio.codec.http11;

import static com.generallycloud.baseio.codec.http11.WebSocketCodec.CHANNEL_KEY_SERVICE_NAME;
import static com.generallycloud.baseio.codec.http11.WebSocketCodec.OP_CONNECTION_CLOSE_FRAME;
import static com.generallycloud.baseio.codec.http11.WebSocketCodec.OP_CONTINUATION_FRAME;
import static com.generallycloud.baseio.codec.http11.WebSocketCodec.TYPE_PING;
import static com.generallycloud.baseio.codec.http11.WebSocketCodec.TYPE_PONG;
import static com.generallycloud.baseio.codec.http11.WebSocketCodec.TYPE_TEXT;

import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.AbstractFrame;
import com.generallycloud.baseio.protocol.Frame;

public class WebSocketFrame extends AbstractFrame implements HttpMessage {

    private byte[]  byteArray;
    private boolean eof;
    private String  readText;
    private String  serviceName;
    private byte    type;

    public WebSocketFrame() {
        this.type = TYPE_TEXT;
    }

    public byte[] getByteArray() {
        return byteArray;
    }

    @Override
    public String getFrameName() {
        return serviceName;
    }

    @Override
    public String getReadText() {
        return readText;
    }

    public int getType() {
        return type;
    }

    public boolean isCloseFrame() {
        return type == OP_CONNECTION_CLOSE_FRAME;
    }

    public boolean isContinuationFrame() {
        return type == OP_CONTINUATION_FRAME;
    }

    public boolean isEof() {
        return eof;
    }

    protected WebSocketFrame reset(String serviceName, int limit) {
        this.byteArray = null;
        this.eof = false;
        this.readText = null;
        this.type = 0;
        this.serviceName = serviceName;
        super.reset();
        return this;
    }

    public void setByteArray(byte[] byteArray) {
        this.byteArray = byteArray;
    }

    public void setEof(boolean eof) {
        this.eof = eof;
    }

    @Override
    public Frame setPing() {
        this.type = TYPE_PING;
        return super.setPing();
    }

    @Override
    public Frame setPong() {
        this.type = TYPE_PONG;
        return super.setPong();
    }

    public void setReadText(String readText) {
        this.readText = readText;
    }

    protected void setServiceName(NioSocketChannel ch) {
        this.serviceName = (String) ch.getAttribute(CHANNEL_KEY_SERVICE_NAME);
    }

    protected void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    protected void setWsType(byte type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return getReadText();
    }

}
