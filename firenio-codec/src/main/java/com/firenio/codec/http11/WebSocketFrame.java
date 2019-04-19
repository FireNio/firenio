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
package com.firenio.codec.http11;

import com.firenio.component.Frame;
import com.firenio.component.Channel;

public class WebSocketFrame extends Frame {

    private boolean eof;
    private byte    type;

    public WebSocketFrame() {
        this(WebSocketCodec.TYPE_TEXT);
    }

    public WebSocketFrame(byte type) {
        this.type = type;
    }

    @Override
    public String getFrameName() {
        throw new UnsupportedOperationException("use getFrameName(ch) instead");
    }

    public String getFrameName(Channel ch) {
        return ((HttpAttachment) ch.getAttachment()).getWebsocketFrameName();
    }

    public int getType() {
        return type;
    }

    public boolean isBinary() {
        return type == WebSocketCodec.TYPE_BINARY;
    }

    public boolean isCloseFrame() {
        return type == WebSocketCodec.TYPE_CLOSE;
    }

    public boolean isContinuationFrame() {
        return type == WebSocketCodec.TYPE_CONTINUE;
    }

    public boolean isEof() {
        return eof;
    }

    @Override
    public boolean isText() {
        return type == WebSocketCodec.TYPE_TEXT;
    }

    @Override
    public WebSocketFrame reset() {
        this.eof = false;
        this.type = 0;
        super.reset();
        return this;
    }

    public void setEof(boolean eof) {
        this.eof = eof;
    }

    public void setType(byte type) {
        this.type = type;
    }

    @Override
    public String toString() {
        if (isText()) {
            return getStringContent();
        }
        return null;
    }

}
