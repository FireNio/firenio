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

    private byte markCode = WebSocketCodec.FIN_EOF;

    public WebSocketFrame() {
        this(WebSocketCodec.TYPE_TEXT);
    }

    public WebSocketFrame(byte opcode) {
        this.setOpcode(opcode);
    }

    @Override
    public String getFrameName() {
        throw new UnsupportedOperationException("use getFrameName(ch) instead");
    }

    public String getFrameName(Channel ch) {
        return ((HttpAttachment) ch.getAttachment()).getWebsocketFrameName();
    }

    public int getOpcode() {
        return markCode & 0xf;
    }

    public boolean isBinary() {
        return getOpcode() == WebSocketCodec.TYPE_BINARY;
    }

    public boolean isCloseFrame() {
        return getOpcode() == WebSocketCodec.TYPE_CLOSE;
    }

    public boolean isContinuationFrame() {
        return getOpcode() == WebSocketCodec.TYPE_CONTINUE;
    }

    @Override
    public boolean isText() {
        return getOpcode() == WebSocketCodec.TYPE_TEXT;
    }

    @Override
    public WebSocketFrame reset() {
        this.setOpcode((byte) (WebSocketCodec.TYPE_TEXT | WebSocketCodec.FIN_EOF));
        super.reset();
        return this;
    }

    public void setFin(byte fin) {
        this.markCode = (byte) ((fin << 4) | getOpcode());
    }

    public void setOpcode(byte opcode) {
        this.markCode = (byte) ((markCode & 0xf0) | (opcode & 0xf));
    }

    public void setMarkCode(byte markCode) {
        this.markCode = markCode;
    }

    public byte getMarkCode() {
        return markCode;
    }
    
    public boolean isEof(){
    	return (markCode & WebSocketCodec.FIN_EOF) != 0;
    }

    @Override
    public String toString() {
        if (isText()) {
            return getStringContent();
        }
        return null;
    }

}
